from flask import Flask, jsonify, request

app = Flask(__name__)

@app.route('/user', methods=['GET', 'POST'])
def get_user():
    args = request.args
    json = request.json
    method = request.method
    resp = {
        "method": method,
        "agent": None
    }
    if bool(args) is True:
        resp.update(args)
    if bool(json) is True:
        resp.update(json)
    return jsonify(resp)

if __name__ == '__main__':
    app.debug = True
    app.run(host='127.0.0.1',port=3000)
