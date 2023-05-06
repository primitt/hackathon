from flask import Flask, request, render_template, redirect, make_response, url_for
import requests
import os
import sys
import hashlib 

app = Flask(__name__)
url = "https://1ccb-2a09-bac1-76c0-c98-00-26b-72.ngrok-free.app"
@app.route("/", methods=["GET"])
def index():
    return render_template("index.html")
@app.route("/login", methods=["GET", "POST"])
def login():
    return render_template("login.html")
@app.route("/signup", methods=["GET", "POST"])
def signup():
    if request.method == "GET":
        return render_template("signup.html")
    if request.method == "POST":
        form = request.form
        print(form)
        if form["password"] == form["rpassword"]:
            if len(form["username"]) < 17:
                if len(form["password"]) > 7:
                    rqp = requests.post(url=url + "/api/register", json={
                        "username":form["username"],
                        "password":hashlib.sha1(form["password"].encode()).hexdigest()
                    })
                    if rqp.text["success"] == "true":
                        resp = make_response(redirect(url_for(survey())))
                        resp.set_cookie("uuid", rqp.text["uuid"])
                        resp.set_cookie("sessionID", rqp.text["sessionId"])
                        return resp
                    else:
                        return render_template("signup.html", message="Unable to sign up, try again") 
                else:
                    return render_template("signup.html", message="Password must be more than 8 characters")
            else:
                return render_template("signup.html", message="Password must be less than")
        else:
            return render_template("signup.html", message="Passwords must equal each other")
@app.run("/survey")
def survey():
    return render_template("survey.html")
if __name__ == "__main__":
    app.run(debug=True)