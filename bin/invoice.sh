#!/bin/bash

# Überprüfen, ob zwei Argumente (year und month) übergeben wurden
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <year> <month>"
    exit 1
fi

# Parameter in Variablen speichern
year=$1
month=$2

# Festgelegtes Host-Verzeichnis für die Dateien
host_directory="/Users/thomas.bitaix/temp/invoices"

# Dynamisches Archiv-Verzeichnis basierend auf year und month
invoiceArchive="/Users/thomas.bitaix/SynologyDrive/01_Verwaltung/${year}/Uplpoad_Addison/${month}/"

# Docker Container starten mit übergebenen Parametern für Jahr und Monat
docker run -v "$host_directory:/data" thomasdocker/invoicebot "$year" "$month"

# Warten, bis das Verzeichnis erstellt und die App im Container ausgeführt wird
sleep 2

# Ordner mit dem Dateimanager öffnen
open "$host_directory"

# Benutzereingabe abfragen
read -p "Sind die Rechnungen in Ordnung? (y/n): " user_input

# Bedingung für die Eingabe prüfen
if [ "$user_input" == "y" ]; then
    # Sicherstellen, dass das Archiv-Verzeichnis existiert
    mkdir -p "$invoiceArchive"

    # Verzeichnis in das Archiv kopieren
    cp -R "$host_directory"/${year}-${month}/* "$invoiceArchive/"

    echo "Die Rechnungen wurden erfolgreich in das Archiv kopiert."

    # Archiv-Verzeichnis im Finder öffnen
    open "$invoiceArchive"

    # Öffnen einer neuen E-Mail in Outlook mit mailto
    email_subject="Rechnungen für ${month}/${year}"
    email_body="Bitte finden Sie die Rechnungen für ${month}/${year} im Anhang."

    # URL-kodierte mailto-URL für Betreff und Body
    open "mailto:empfaenger@example.com?subject=$(echo $email_subject | sed 's/ /%20/g')&body=$(echo $email_body | sed 's/ /%20/g')"

elif [ "$user_input" == "n" ]; then
    echo "Vorgang abgebrochen. Die Rechnungen wurden nicht archiviert."
else
    echo "Ungültige Eingabe. Vorgang abgebrochen."
fi
