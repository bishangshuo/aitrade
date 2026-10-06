#pragma once

#include <string>
#include <queue>
#include <mutex>
#include <thread>
#include <condition_variable>
#include <atomic>


class RabbitPublisher
{
public:
    static RabbitPublisher& Instance();

    bool Start(
        const std::string& host,
        int port,
        const std::string& user,
        const std::string& password,
        const std::string& exchange,
        const std::string& routingKey
    );
    void Publish(
        const std::string& json
    );
    void Stop();
private:
    RabbitPublisher();
    ~RabbitPublisher();
    RabbitPublisher(const RabbitPublisher&) = delete;
    RabbitPublisher& operator=(const RabbitPublisher&) = delete;
private:
    void Worker();
    bool Connect();
private:
    void* m_conn = nullptr;
    std::string m_host;
    int m_port{};
    std::string m_user;
    std::string m_password;
    std::string m_exchange;
    std::string m_routingKey;

    std::queue<std::string> m_queue;
    std::mutex m_mutex;
    std::condition_variable m_cv;
    std::thread m_thread;
    std::atomic<bool> m_running{ false };
};