from flask import Flask, request, render_template, redirect, make_response, url_for
import requests
import os
import json
import sys
import hashlib 

app = Flask(__name__)
url = "https://263f-2a09-bac1-76a0-c98-00-26b-94.ngrok-free.app/"
def sessionReq(session, uuid):
    rqq = requests.post(url=url + "/api/session", json={
        "uuid":uuid,
        "sessionId":session
    })
    txt = json.loads(rqq.text)
    if txt["success"] == "true":
        return True
    else:
        return False
@app.route("/", methods=["GET"])
def index():
    return render_template("index.html")
@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        if sessionReq(request.cookies.get("sessionID"), request.cookies.get("uuid")):
            return render_template("home.html")
        else:
            bdy = request.form
            print(bdy)
            rqq = requests.post(url=url + "/api/login", json={
            "username":bdy["username"],
            "password":hashlib.sha1(bdy["password"].encode()).hexdigest()
        })
        txt = json.loads(rqq.text)
        if txt["success"] == "true":
            resp = make_response(redirect(url_for("home")))
            resp.set_cookie("uuid", txt["uuid"])
            resp.set_cookie("sessionID", txt["sessionId"])
        else:
            return render_template("login.html", message="Your credentials are incorrect, please try again. ")
    else:
        if sessionReq(request.cookies.get("sessionID"), request.cookies.get("uuid")):
            return redirect(url_for("home"))
        else:
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
                    txt = json.loads(rqp.text)
                    if txt["success"] == "true":
                        resp = make_response(redirect(url_for("survey")))
                        resp.set_cookie("uuid", txt["uuid"])
                        resp.set_cookie("sessionID", txt["sessionId"])
                        return resp
                    else:
                        return render_template("signup.html", message="Unable to sign up, try again") 
                else:
                    return render_template("signup.html", message="Password must be more than 8 characters")
            else:
                return render_template("signup.html", message="Password must be less than")
        else:
            return render_template("signup.html", message="Passwords must equal each other")
@app.route("/survey", methods=["GET", "POST"])
def survey():
    if request.method == "POST":
        allergy_array = []
        txt = request.form
        try:
            if txt["Peanuts"]:
                allergy_array.append("Peanuts")
        except:
            pass
        try:
            if txt["Tree Nuts"]:
                allergy_array.append("TreeNuts")
        except:
            pass
        try:
            if txt["Dairy"]:
                allergy_array.append("Dairy")
        except:
            pass
        try:
            if txt["Eggs"]:
                allergy_array.append("Eggs")
        except:
            pass
        try:
            if txt["Wheat"]:
                allergy_array.append("Wheat")
        except:
            pass
        try:
            if txt["Soy"]:
                allergy_array.append("Soy")
        except:
            pass
        try:
            if txt["Fish"]:
                allergy_array.append("Fish")
        except:
            pass
        try:
            if txt["Shellfish"]:
                allergy_array.append("Shellfish")
        except:
            pass
        try:
            if txt["other"]:
                otherssplit = txt["otherAllergies"].split(",")
                for values in otherssplit:
                    allergy_array.append(values)
        except:
            pass
        try:
            if txt["diet"]:
                diet = txt["diet"]
                if diet == "Select your diet": 
                    diet = ""
            else:
                diet = ""
        except:
            pass
        rqp = requests.post(url=url + "/api/survey", json={
            "allergies":allergy_array,
            "diet":diet,
            "uuid":request.cookies.get("uuid")
        })
        print(rqp.text)
        if rqp.text == "success":
            return redirect("home")
        else: 
            return redirect("index")
    if request.method == "GET":
        if sessionReq(request.cookies.get("sessionID"), request.cookies.get("uuid")):
            return render_template("survey.html")
        else:
            return redirect(url_for("login"))
@app.route("/home", methods=["GET", "POST"])
def home():
    if request.method == "GET":
        if sessionReq(request.cookies.get("sessionID"), request.cookies.get("uuid")):
            return render_template("home.html")
        else:
            return redirect(url_for("login"))
    else:
        if sessionReq(request.cookies.get("sessionID"), request.cookies.get("uuid")):
            fr = request.form
            rqs = requests.post(url = url + "/api/search", json={
                "preptime":int(fr["prep-time"]),
                "searchquery":fr["desired-meal"],
                "uuid":request.cookies.get("uuid")
            })
            dumper = json.loads(rqs.text)
            print(dumper["results"][0])
            return render_template("home.html", results=dumper["results"])
        else:
            return redirect(url_for("login"))
@app.route("/recipie/<id>", methods=["GET"])
def recipie(id):
    rqs = requests.post(url = url + "/api/recipe", json={
        "id":int(id),
    })
    dumper = json.loads(rqs.text)
    ingredients = []
    for values in dumper["extendedIngredients"]:
        ingredients.append(values["original"])
    intruction = dumper["instructions"]
    print(ingredients)
    print(intruction)
    return render_template("recipe.html", ingredients=ingredients, intruction=intruction, title=dumper["title"], rn=dumper["readyInMinutes"], summary=dumper["summary"] )
if __name__ == "__main__":
    app.run(debug=True, host="127.0.0.1")