import ddddocr
import base64

def get_slider_distance(background_b64, slider_b64):
    """
    使用 ddddocr 计算滑块到缺口的水平距离
    
    :param background_b64: 带缺口的背景图 Base64 字符串
    :param slider_b64: 滑块图 Base64 字符串
    :return: 缺口左上角的 X 坐标（水平距离）
    """
    background_bytes = base64.b64decode(background_b64)
    slider_bytes = base64.b64decode(slider_b64)
    
    # 初始化 ddddocr，关闭 OCR 和检测功能，专注滑块匹配
    det = ddddocr.DdddOcr(det=False, ocr=False, show_ad=False)
    
    # 执行缺口匹配
    result = det.slide_match(slider_bytes, background_bytes)
    print("识别结果：result=")
    print(result)
    
    # 提取缺口左上角 X 坐标
    target_x = result['target'][0]
    
    return target_x

def image_file_to_base64(file_path):
    with open(file_path, 'rb') as f:
        return base64.b64encode(f.read()).decode('utf-8')

bg_b64 = image_file_to_base64('debug_bgImage.png')
slider_b64 = image_file_to_base64('slider.png')

distance = get_slider_distance(bg_b64, slider_b64)
print("缺口距离：")
print(distance)