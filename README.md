# 🚀 Force Item Battle

![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green)
![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot-blue)
![Java](https://img.shields.io/badge/Java-17+-orange)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)

> ⚔️ Ein modernes Team-Minispiel, bei dem Spieler zufällige Items so schnell wie möglich finden müssen.

---

## ✨ Features

- 👥 Team-System (bis zu 8 Teams)  
- 🟥 Skip-System mit Limit  
- 🎒 Optionales Team-Backpack  
- 🔒 Team-Lock (kein Wechsel im Spiel)  
- ⏱ Timer mit BossBar + ActionBar  
- 🧠 Item über dem Kopf (ArmorStand-System)  
- ⚡ Ultra-smooth Anzeige (20 TPS)  
- 🧰 Admin GUI für alle Settings  

---

## 🎮 Gameplay

1. Starte das Spiel mit `/forceitembattle start`
2. Spieler bekommen ein Ziel-Item
3. Ziel:
   > Finde das Item schneller als die anderen Teams
4. Wenn ein Spieler das Item hat:
   - neue Aufgabe wird generiert
5. Teams arbeiten gemeinsam

---

## 🛠 Commands

```bash
/forceitembattle start
/forceitembattle stop
/forceitembattle restart
/forceitembattle settings
```

---

## 🔐 Permissions

```bash
fib.start
```

Default: `op`

---

## ⚙️ Config

```yaml
challenge-seconds: 300

max-skips: 3

enable-backpack: true

team-lock: false

blacklist:
  - BEDROCK
  - COMMAND_BLOCK
  - BARRIER

teams:
  team1: []
  team2: []
  team3: []
  team4: []
  team5: []
  team6: []
  team7: []
  team8: []
```

---

## 📦 Installation

1. Lade die `.jar` herunter  
2. Lege sie in den `/plugins` Ordner  
3. Starte den Server  
4. Fertig ✅  

---

## 🧠 Besonderheiten

- ✔ Kein Scoreboard → cleaner Look  
- ✔ BossBar + ActionBar kombiniert  
- ✔ Echtzeit Item-Anzeige über Spielern  
- ✔ Sehr performantes System  

---

## 🛣 Roadmap

- [ ] 🏆 Punktesystem  
- [ ] 🎯 Scoreboard (optional)  
- [ ] 🔊 Sounds & Effekte  
- [ ] 🌍 Mehrsprachigkeit  
- [ ] 🎮 verschiedene Modi  

---

## 👨‍💻 Entwickler

**Ben Spiel**

---

## ⭐ Support

Wenn dir das Plugin gefällt:

👉 ⭐ Repo auf GitHub  
👉 Issues für Bugs / Ideen  

---

## 📜 Lizenz

MIT License
