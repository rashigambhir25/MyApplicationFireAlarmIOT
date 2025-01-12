import json
import subprocess
from http.server import BaseHTTPRequestHandler, HTTPServer


class RequestHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        # Read the length of the POST data
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)

        # Parse the JSON data
        data = json.loads(post_data.decode('utf-8'))
        # Run the command to turn on the light
        subprocess.run(["python", "onSwitch.py"])  # Adjust as needed
        self.send_response(200)


        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(b'{"message": "Light is turned on!!!"}')



def run(server_class=HTTPServer, handler_class=RequestHandler, port=5000):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print(f'Starting http server on port {port}')
    httpd.serve_forever()

if __name__ == "__main__":
    run()
