# a project I made for fun


# ChatLAN - Local Network Messenger

Aplikasi chat real-time antar komputer dalam jaringan LAN (Local Area Network) menggunakan Java Swing.

##  Cara Menjalankan

### Kompilasi
```bash
javac -d bin -encoding UTF-8 src/chatlan/model/Message.java src/chatlan/server/ChatServer.java src/chatlan/client/ChatClient.java src/chatlan/ui/ServerUI.java src/chatlan/ui/ClientUI.java src/chatlan/ChatLauncher.java
```

### Jalankan
```bash
java -cp bin chatlan.ChatLauncher
```

## 📖 Cara Penggunaan

### 1. Mulai Server
1. Buka aplikasi dan pilih **"Start Server"**
2. Masukkan port (default: `5000`)
3. Klik **"Start Server"**
4. Catat **IP Address** yang ditampilkan (contoh: `192.168.1.100:5000`)

### 2. Gabung sebagai Client
1. Buka aplikasi di komputer lain dalam jaringan yang sama
2. Pilih **"Join Chat"**
3. Masukkan:
   - **Username** - Nama kamu
   - **Server IP** - IP dari komputer server (contoh: `192.168.1.100`)
   - **Port** - Port yang sama dengan server (contoh: `5000`)
4. Klik **"Connect to Server"**

### 3. Mengirim Pesan
- **Chat Biasa**: Ketik pesan dan tekan Enter atau klik "Kirim"
- **Private Message**: Ketik `/pm username pesan` untuk mengirim pesan pribadi

##  Struktur Projectnya

```
src/
├── chatlan/
│   ├── ChatLauncher.java       # Entry point, launcher screen
│   ├── model/
│   │   └── Message.java        # Model data pesan
│   ├── server/
│   │   └── ChatServer.java     # Server logic
│   ├── client/
│   │   └── ChatClient.java     # Client logic
│   └── ui/
│       ├── ServerUI.java       # GUI untuk server
│       └── ClientUI.java       # GUI untuk client
```

##  Requirements

- Java JDK 8 atau lebih baru
- Komputer harus terhubung dalam jaringan LAN yang sama

##  Catatan

- Pastikan firewall mengizinkan koneksi pada port yang digunakan
- Server dan semua client harus berada dalam subnet yang sama
- Username harus unik untuk setiap client
