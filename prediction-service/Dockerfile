FROM python:3.9-slim

WORKDIR /app

RUN apt-get update && apt-get install -y libpq-dev gcc && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
# Add fastapi and uvicorn if not in requirements, but safe to add explicit install command for now ensuring we have them
RUN pip install --no-cache-dir -r requirements.txt && pip install fastapi uvicorn

COPY . .

# Run uvicorn server on port 8000, accessible externally
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
