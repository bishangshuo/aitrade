#include "pch.h"

#include "RabbitPublisher.h"
extern "C"
{
#include <amqp.h>
#include <amqp_tcp_socket.h>
#include <amqp_framing.h>
}

RabbitPublisher& RabbitPublisher::Instance()
{
	static RabbitPublisher instance;
	return instance;
}

RabbitPublisher::RabbitPublisher()
{
}

RabbitPublisher::~RabbitPublisher()
{
	Stop();
}

bool RabbitPublisher::Start(
	const std::string& host,
	int port,
	const std::string& user,
	const std::string& password,
	const std::string& exchange,
	const std::string& routingKey
)
{
	m_host = host;
	m_port = port;
	m_user = user;
	m_password = password;
	m_exchange = exchange;
	m_routingKey = routingKey;
	if (!Connect())
		return false;
	m_running = true;
	m_thread = std::thread(
		&RabbitPublisher::Worker,
		this
	);
	return true;
}

bool RabbitPublisher::Connect()
{
	auto conn = amqp_new_connection();
	auto socket = amqp_tcp_socket_new(conn);

	if (!socket)
		return false;

	if (
		amqp_socket_open(
			socket,
			m_host.c_str(),
			m_port
		)
		)
	{
		return false;
	}

	amqp_rpc_reply_t reply;

	reply = amqp_login(
		conn,
		"/",
		0,
		131072,
		0,
		AMQP_SASL_METHOD_PLAIN,
		m_user.c_str(),
		m_password.c_str()
	);

	if (reply.reply_type != AMQP_RESPONSE_NORMAL)
	{
		return false;
	}

	amqp_channel_open(
		conn,
		1
	);

	reply = amqp_get_rpc_reply(conn);

	if (reply.reply_type != AMQP_RESPONSE_NORMAL)
		return false;

	m_conn = conn;

	return true;
}

void RabbitPublisher::Publish(
	const std::string& json
)
{
	{
		std::lock_guard<std::mutex> lock(
			m_mutex
		);

		m_queue.push(json);
	}

	m_cv.notify_one();
}

void RabbitPublisher::Worker()
{
	while (m_running)
	{
		std::string msg;

		{
			std::unique_lock<std::mutex> lock(
				m_mutex
			);

			m_cv.wait(
				lock,
				[&]
				{
					return
						!m_queue.empty()
						||
						!m_running;
				}
			);

			if (!m_running)
				break;

			msg =
				std::move(
					m_queue.front()
				);

			m_queue.pop();
		}

		amqp_bytes_t message;

		message.len = msg.size();

		message.bytes = (void*)msg.data();

		int sendRes = amqp_basic_publish(
			(amqp_connection_state_t)m_conn,
			1,
			amqp_cstring_bytes(
				m_exchange.c_str()
			),
			amqp_cstring_bytes(
				m_routingKey.c_str()
			),
			1,
			0,
			nullptr,
			message
		);

		//不用等待确认，直接发送即可
		//amqp_frame_t frame;
		//amqp_simple_wait_frame((amqp_connection_state_t)m_conn, &frame);

		if (sendRes == AMQP_STATUS_OK) {
			TRACE("send ok");
		}
		else {
			TRACE("send failed");
		}
	}
}

void RabbitPublisher::Stop()
{
	m_running = false;

	m_cv.notify_all();

	if (m_thread.joinable())
		m_thread.join();

	if (m_conn)
	{
		amqp_channel_close(
			(amqp_connection_state_t)m_conn,
			1,
			AMQP_REPLY_SUCCESS
		);

		amqp_connection_close(
			(amqp_connection_state_t)m_conn,
			AMQP_REPLY_SUCCESS
		);

		amqp_destroy_connection(
			(amqp_connection_state_t)m_conn
		);

		m_conn = nullptr;
	}
}