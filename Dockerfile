FROM gradle:8.1.1-jdk17
LABEL authors="bungleberrysquinglebobby"
WORKDIR /app
COPY ./backend ./backend
WORKDIR /app/backend
RUN ./gradlew run

FROM python:latest

COPY ./frontend ./frontend
WORKDIR /frontend

CMD ["python", "-m", "pip", "install", "flask", "requests"]
EXPOSE 8432
RUN python main.py