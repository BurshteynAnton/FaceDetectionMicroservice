from flask import Flask, render_template, request, jsonify
import requests
import cv2
import os
from PIL import Image
from dotenv import load_dotenv

load_dotenv()

JAVA_BACKEND_URL = os.getenv("JAVA_BACKEND_URL")
FLASK_HOST = os.getenv("FLASK_HOST", "0.0.0.0")
FLASK_PORT = int(os.getenv("FLASK_PORT", 5000))

app = Flask(__name__)
UPLOAD_FOLDER = 'static/uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route('/')
def photo_user():
    return render_template('photo_user.html')

@app.route('/managment_panel')
def managment_panel():
    return render_template('managment_panel.html')

@app.route('/upload', methods=['POST'])
def upload_image():
    if 'file' not in request.files:
        return jsonify({'error': 'No file part'}), 400

    file = request.files['file']
    name = request.form.get('name', '').strip()

    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    if not name:
        return jsonify({'error': 'Name is required'}), 400

    file_path = os.path.join(UPLOAD_FOLDER, file.filename)
    file.save(file_path)

    try:
        with open(file_path, "rb") as f:
            response = requests.post(
                f"{JAVA_BACKEND_URL}/photos/upload",
                files={"file": (file.filename, f, "image/jpeg")},
                data={"name": name}
            )
        if response.status_code == 200:
            return jsonify({'message': 'Photo sent successfully!'})
        else:
            return jsonify({'error': f"Server error: {response.text}"}), 500
    except Exception as e:
        return jsonify({'error': f"Can't send Photo: {e}"}), 500

@app.route('/capture', methods=['POST'])
def capture_photo():
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        return jsonify({'error': 'Unable to access the webcam'}), 500

    ret, frame = cap.read()
    if not ret:
        return jsonify({'error': 'Failed to capture image'}), 500

    file_path = os.path.join(UPLOAD_FOLDER, 'captured_photo.jpg')
    cv2.imwrite(file_path, frame)
    cap.release()
    return jsonify({'message': 'Photo captured successfully!', 'file_path': file_path})

@app.route('/api/register', methods=['POST'])
def proxy_register():
    response = requests.post(f"{JAVA_BACKEND_URL}/auth/register", json=request.get_json())
    return jsonify(response.json()), response.status_code

@app.route('/api/login', methods=['POST'])
def proxy_login():
    response = requests.post(f"{JAVA_BACKEND_URL}/auth/authenticate", json=request.get_json())
    return jsonify(response.json()), response.status_code

@app.route('/api/photos/search/<int:photo_id>', methods=['GET'])
def proxy_search_photo(photo_id):
    response = requests.get(f"{JAVA_BACKEND_URL}/photos/search/{photo_id}")
    return response.text, response.status_code

@app.route('/api/photos/delete/<int:photo_id>', methods=['DELETE'])
def proxy_delete_photo(photo_id):
    headers = {}
    if "Authorization" in request.headers:
        headers["Authorization"] = request.headers["Authorization"]
    response = requests.delete(f"{JAVA_BACKEND_URL}/photos/delete/{photo_id}", headers=headers)
    return "", response.status_code

@app.route('/api/photos/list', methods=['GET'])
def proxy_list_photos():
    response = requests.get(f"{JAVA_BACKEND_URL}/photos/list")
    return jsonify(response.json()), response.status_code

if __name__ == '__main__':
    app.run(debug=True, host=FLASK_HOST, port=FLASK_PORT)
