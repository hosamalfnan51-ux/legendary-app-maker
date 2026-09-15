import http.server
import socketserver
import os

PORT = 8080
DIRECTORY = "."

class MyHttpRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    # ميزة ذكية لمنع تجميد السيرفر وتحديث البيانات تلقائياً للمستخدم
    def end_headers(self):
        self.send_header('Cache-Control', 'no-store, no-cache, must-revalidate')
        super().end_headers()

def start_server():
    # التأكد من وجود ملف تجريبي للمعاينة حتى لا يظهر المتصفح فارغاً
    if not os.path.exists("index.html"):
        with open("index.html", "w", encoding="utf-8") as f:
            f.write("""<!DOCTYPE html>
<html>
<head>
    <meta charset='utf-8'>
    <meta name='viewport' content='width=device-width, initial-scale=1.0'>
    <title>معاينة تطبيقك الأسطوري</title>
    <style>
        body { background-color: #000000; color: #FFD700; font-family: sans-serif; text-align: center; padding-top: 50px; }
        .box { border: 2px solid #FFD700; padding: 20px; display: inline-block; border-radius: 10px; background-color: #111; }
    </style>
</head>
<body>
    <div class='box'>
        <h1>صانع التطبيقات الأسطوري 👑</h1>
        <p>جاري استقبال تطبيقك أو لعبتك القادمة من الذكاء الاصطناعي...</p>
    </div>
</body>
</html>""")

    handler_object = MyHttpRequestHandler
    
    # السماح بإعادة استخدام المنفذ فوراً لتجنب خطأ تعطل السيرفر المزعج
    socketserver.TCPServer.allow_reuse_address = True
    
    with socketserver.TCPServer(("", PORT), handler_object) as httpd:
        print(f"[+] مبروك! خادم المعاينة الحية يعمل الآن بنجاح على المنفذ: {PORT}")
        print("[+] يمكنك فتح المتصفح على هاتف المستخدم برابط: http://localhost:8080")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n[-] تم إيقاف خادم المعاينة الحية بنجاح.")
            httpd.server_close()

if __name__ == "__main__":
    start_server()
