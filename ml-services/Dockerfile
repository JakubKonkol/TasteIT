FROM python:3.12-slim

WORKDIR /app

# Instalacja niezbędnych pakietów systemowych
RUN apt-get update && apt-get install -y \
    build-essential \
    python3-dev \
    python3-setuptools \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Kopiowanie plików requirements
COPY requirements.txt .

# Aktualizacja pip i podstawowych narzędzi
RUN pip install --no-cache-dir --upgrade pip setuptools wheel

# Instalacja numpy 2.0
RUN pip install --no-cache-dir 'numpy>=2.0.0,<3.0.0'

# Instalacja podstawowych zależności ML
RUN pip install --no-cache-dir \
    blis==1.0.1 \
    scipy==1.13.1 \
    scikit-learn==1.5.2

# Instalacja spacy i zależności
RUN pip install --no-cache-dir \
    spacy==3.8.2 \
    spacy-legacy==3.0.12 \
    spacy-loggers==1.0.5

# Instalacja gensim
RUN pip install --no-cache-dir gensim==4.3.3

# Instalacja Kafki
RUN pip install --no-cache-dir kafka-python-ng==2.2.3

# Instalacja pozostałych zależności
RUN pip install --no-cache-dir \
    pandas==2.2.3 \
    requests==2.32.3 \
    python-dotenv==1.0.1 \
    pymongo==4.9.2

# Instalacja modelu spacy
RUN python -m spacy download en_core_web_sm

# Kopiowanie kodu aplikacji
COPY . .

# Uruchomienie aplikacji
CMD ["python", "main.py"]