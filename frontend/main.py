from flask import Flask, request, render_template
import requests
import os
import sys
import hashlib 

app = Flask(__name__)

@app.route("/", method=["GET"])
def index():
    return render_template("index.html")
