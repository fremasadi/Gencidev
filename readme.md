# GenciDev Test - Android E-commerce App

## 📱 Overview

Genci Dev Test adalah aplikasi e-commerce Android yang dibangun menggunakan **Jetpack Compose**, **Hilt Dependency Injection**, **Room Database**, dan **Retrofit**. Aplikasi ini menggunakan **DummyJSON API** sebagai backend dan menerapkan **offline-first architecture** dengan sistem caching yang robust.

## ✨ Fitur Utama

### 🔐 Authentication
- **Login**: Sistem autentikasi menggunakan DummyJSON API
- **Auto-login**: Cek status login otomatis saat aplikasi dibuka
- **Logout**: Keluar dari akun dengan pembersihan data lokal
- **Session Management**: Menggunakan DataStore untuk penyimpanan token

### 🏠 Home (Product Catalog)
- **Product List**: Menampilkan hingga 30 produk dari DummyJSON API
- **Offline Support**: Cache produk di Room Database untuk akses offline
- **Product Categories**: Filter produk berdasarkan kategori
- **Search Functionality**: Pencarian produk lokal dan online
- **Product Detail**: Informasi lengkap produk dengan gambar

### 🛒 Shopping Cart
- **Cart Management**: Menampilkan semua cart (hingga 30 cart)
- **Add to Cart**: Tambah produk ke keranjang belanja
- **Cart Details**: Informasi detail produk, quantity, harga, dan diskon
- **Local Caching**: Cart disimpan secara lokal untuk akses cepat

### 👤 User Profile  
- **Profile Display**: Menampilkan data profil pengguna yang login
- **User Information**: Nama, email, username, dan foto profil
- **Persistent Storage**: Data profil disimpan menggunakan Room Database

### 🚀 Splash Screen
- **Initial Check**: Pengecekan status login saat aplikasi dimulai
- **Smooth Transition**: Navigasi otomatis ke halaman yang sesuai

## 🏗️ Arsitektur & Teknologi

### Architecture Pattern
- **MVVM (Model-View-ViewModel)**: Pemisahan concern yang jelas
- **Clean Architecture**: Domain, Data, dan Presentation layer
- **Repository Pattern**: Abstraksi data source
- **UseCase Pattern**: Business logic terisolasi

### Tech Stack
- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Hilt
- **Local Database**: Room Database
- **Network**: Retrofit + OkHttp
- **Async Programming**: Kotlin Coroutines + Flow
- **Image Loading**: Coil
- **Data Storage**: DataStore (Preferences)

### Offline-First Strategy
- **Smart Caching**: Data disimpan lokal dengan timestamp
- **Network Detection**: Deteksi koneksi internet otomatis
- **Fallback Mechanism**: Gunakan cache saat offline
- **Cache Expiration**: Automatic cache refresh (5 menit untuk produk, 3 menit untuk cart)

## 🔧 Instalasi & Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) atau lebih baru
- Android SDK 24 - 36
- JDK 17

### Langkah Instalasi

1. **Clone Repository**
   ```bash
   git clone https://github.com/fremasadi/Gencidev
   cd gencidevtest
   ```

2. **Open di Android Studio**
   - Buka Android Studio
   - Pilih "Open" dan navigasi ke folder project
   - Tunggu Gradle sync selesai

3. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   atau gunakan tombol Run di Android Studio

## 🌐 API Integration

### Base URL
```
https://dummyjson.com/
```

### Endpoints yang Digunakan

#### Authentication
- `POST /auth/login` - Login pengguna

#### Products  
- `GET /products` - List semua produk (limit: 30)
- `GET /products/{id}` - Detail produk berdasarkan ID
- `GET /products/search` - Pencarian produk
- `GET /products/categories` - List kategori produk
- `GET /products/category/{category}` - Produk berdasarkan kategori

#### Shopping Cart
- `GET /carts` - List semua cart (limit: 30) 
- `POST /carts/add` - Tambah produk ke cart


## 🎯 Cara Penggunaan

### 1. Login
- Gunakan kredensial dari DummyJSON API
- Contoh: username: `kminchelle`, password: `0lelplR`
- Atau kredensial DummyJSON lainnya

### 2. Navigasi Home
- Browse produk yang tersedia
- Gunakan search untuk mencari produk spesifik
- Filter berdasarkan kategori
- Tap produk untuk melihat detail

### 3. Shopping Cart
- Lihat semua cart yang tersedia di sistem
- Tambah produk ke cart dari halaman detail
- Kelola quantity dan lihat total harga

### 4. Profile
- Lihat informasi profil pengguna
- Data otomatis tersimpan saat login
- Logout untuk keluar dari akun

## 🔄 Caching Strategy

### Product Caching
- **Cache Duration**: 5 menit
- **Strategy**: Network-first dengan fallback ke cache
- **Update**: Otomatis refresh jika cache expired

### Cart Caching  
- **Cache Duration**: 3 menit (lebih pendek karena data lebih dinamis)
- **Strategy**: Smart caching dengan network detection
- **Sync**: Real-time update saat menambah ke cart

### User Data Caching
- **Storage**: Room Database + DataStore
- **Persistence**: Data tersimpan hingga logout
- **Access**: Flow-based reactive updates

### Offline Fallback
- Otomatis gunakan cache saat offline
- Network state observer dengan Flow
- Graceful degradation tanpa crash

## 🛠️ Development Features

### Logging
- HTTP request/response logging dengan OkHttp Interceptor
- Room database query logging
- Network state change logging

### Error Handling
- Comprehensive try-catch blocks
- Network error fallback ke cache
- User-friendly error messages

### Performance
- Lazy loading untuk lists
- Image caching dengan Coil
- Database queries dioptimasi dengan indices

## 👨‍💻 Developer

Dibuat frecode dengan ❤️ untuk test development skills

---

**Note**: Aplikasi ini menggunakan DummyJSON API yang merupakan fake API untuk testing dan development. Data yang ditampilkan bukanlah data produk real.