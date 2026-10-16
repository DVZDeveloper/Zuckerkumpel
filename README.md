Installationsguide:

Ich nehme kurz vorweg: 
Achte bei allen Eingaben auf dem Computer auf korrekte Gross-Kleinschreibung und auf Leerzeichen!

Auf deinem Handy:
Öffne deine Freestyle-Libre-3-App
Gehe zu Verbundene Apps -> Libre-Link-Up -> Verbindung hinzufügen
Fülle das Formular aus

Du sollteste bald eine E-Mail bekommen, befolge ab dann die Anweisungen der App bis du deine Glukosewerte in deriner LibreLinkUp-App hast.
Wir sind mit dem Handy jetzt fertig, du kannst es auf die Seite legen.

Auf dem Computer:
Lade die neuste APK der App unter Releases herunter
Lade die neuste Version der Platform Tools für Android herunter
  https://developer.android.com/tools/releases/platform-tools

Extrahiere die ZIP-Datei und öffne den neu erstellten Ordner

Drücke Rechtsclick -> als Pfad kopieren

Öffne die Suchleiste und gib "cmd" ein, drücke auf "Eingabeaufforderung"

Gebe `cd ` mit dem Leerschlag in das Feld ein und clicke einmal mit dem Rechtsklick auf das Feld. 
Es sollte ungefähr so aussehen:
  `cd "C:\Users\DeinName\Downloads\platform-tools-latest-windows\platform-tools"`
Drücke die Enter-Taste.

Behalte dieses Fenster offen, du brauchst es später nocheinmal!

Auf der Uhr:
Überprüfe zuerst unter 
  Einstellungen -> Konnektivität -> WLAN 
ob die Uhr mit dem WLAN verbunden ist und ob es das gleiche Netzwerk wie der Computer ist

Navigiere zu Einstellungen -> System -> Info -> Versionen -> Build-Nummer
Drücke 7x auf diese Nummer. Eventuell wirst du aufgefordert, deine Uhr zu entsperren
Danach sollte ein Popup mit "Du bist jetzt ein Entwickler!" auftauchen

Navigiere zurück, bis du wieder auf der ersten Seite der Einstellungen bist

Eine neue Option "Entwickleroptionen" sollte aufgetaucht sein

Öffne diese und schalte "ADB-Debugging" ein

Navigiere zu "Debugging über WLAN" und schalte dieses ein.
Tippe auf "Neues Gerät koppeln"

Ein WLAN-Kopplungscode und einige IP-Adressen sollten in diesem Menü angezeigt werden.
Behalte dieses Fenster noch offen!

Wieder auf dem Computer:
Gebe `adb pair ` in das Eingabefeld ein. Tippe danach die unter IPv4 angezeigten Text an.
Es sollte ungefähr so aussehen
  `adb pair xxx.xxx.xx.xxx:xxxxx`
Drücke die Enter-Taste

Gebe den WLAN-Kopplungscode in das Eingabefeld ein
Drücke die Enter-Taste

Behalte das Fenster öffen!

Auf der Uhr:
Navigiere eine Ebene zurück und drücke auf "Geräteadressen und Port"
Hier sollten wieder IP-Adressen angezeigt werden, die Zahl nach dem Doppelpunkt sollte aber anders sein als vorher

Behalte das Fenster offen!

Auf dem Computer:
Gebe `adb connect ` in das Eingabefeld ein. Tippe danach die unter IPv4 angezeigten Text an.
Es sollte ungefähr so aussehen
  `adb pair xxx.xxx.xx.xxx:yyyyy`
Drücke die Enter-Taste

Kontrolliere mittels `adb devices` ob die Verbindung funktioniert hat
Die IP sollte nun dort angezeigt werden

Behalte das Fenster offen, navigiere im Dateiexplorer zu der heruntergeladenen APK-Datei und drücke Rechtsklick -> als Pfad kopieren

Zurück in der Eingabeaufforderung, gebe
  `adb -e install ` + Rechtsklick
ein. Die Leerschläge sind wieder zu beachten
Das sollte dann so aussehen:
  `adb install "C:\Users\DeinName\Downloads\Zuckerkumpel_xxxxxxxx.apk"

Warte, bis "Success" in der Eingabeaufforderung erscheint, dann hast du die App installiert!
Noch ganz fertig sind wir aber nicht.

Führe als letztes noch diese Aufforderung aus:
  `adb shell dumpsys deviceidle whitelist +com.zuckerkumpel.app`

Damit sind wir auf dem Computer fertig. Du kannst diesen jetzt auf die Seite legen.

Auf der Uhr:
Jetzt müssen wir noch einige Einstellungen der App konfigurieren

Navigiere zu Einstellungen -> Apps & Benachrichtigungen -> App-Info -> Zuckerkumpel
Unter "Berechtigungen": 
  erlaube Benachrichtigungen
  deaktiviere "App-Aktivität bei Nichtnutzung stoppen"
Unter "Erweitert":
  aktiviere "Wecker und Erinnerungen einrichten erlauben"

Gehe zurück auf den Home-Bildschirm, dann öffne Zuckerkumpel ganz normal über den App-Launcher
  (drücke auf die Krone und Scrolle ganz nach unten)
Scrolle nach unten zu "Login" und gebe deine E-Mail und dein Passwort des oben erstellten LibreLinkUp-Accountes ein
  Mache nutzen von der Spracheingabe und lass Leerzeichen einfach stehen, diese werden sowieso entfernt
  Achte beim Passwort auf Gross-Kleinschreibung!

Zu guter letzt:
Gehe zurück auf den Home-Bildschirm, wische einmal zur Seite um zu den Kacheln zu gelangen.
Halte lang gedrückt, dann wische das Menü ganz nach rechts bis die Option "Neue hinzufügen" auftaucht
Drücke auf das Plus und wähle die Kachel "Aktueller Blutzucker" aus.
Die angezeigten Daten sind jetzt vielleicht noch falsch, da sie noch nicht aktualisieren konnten.
Du kannst sie jederzeit durch öffnen der Einstellungen aktualisieren lassen.

Damit ist der Installationsprozess abgeschlossen. 

Ich hoffe, Zuckerkumpel kann deinen Alltag ein wenig verbessern!
