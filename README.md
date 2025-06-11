# 🏟️ TeamUp - Halı Saha Maçlarını Kolayca Planla ve Katıl!

TeamUp, futbol severlerin mobil cihazları üzerinden kolayca maç organize edebileceği, oyuncu bulabileceği ve halı saha topluluğunu genişletebileceği bir Android uygulamasıdır. Uygulama, kullanıcı dostu arayüzü ve Firebase tabanlı altyapısıyla modern ve esnek bir maç yönetimi deneyimi sunar.

---

## 🚀 Özellikler

- 🔐 Firebase Authentication ile güvenli kullanıcı girişi ve kaydı  
- 👤 Kapsamlı kullanıcı profili: İsim, pozisyon, boy, kilo, doğum tarihi ve profil fotoğrafı (Base64 ile)
- 📍 Konum tabanlı oyuncu keşfi (ilçe bazlı filtreleme)
- 🏁 Maç oluşturma ve maç listesi yönetimi
- 👥 Maç kadrosu görüntüleme ve yönetme
- 🎯 Oyuncuya davet gönderme ve gelen davetleri yönetme (İstek Kutusu)
- 🤝 “Katılmak istiyorum” özelliği ile aktif maçlara istek gönderme
- 🌐 Çoklu dil desteği (anlık dil değişimi - `CompositionLocal` ile)
- 🌙 Açık/Koyu tema desteği (`PreferencesManager` ile kalıcı tema)
- 🔄 Firestore senkronizasyonu ile gerçek zamanlı güncellemeler
- 🧠 Offline veri önbellekleme (SharedPreferences desteği)

---

## 🧱 Kullanılan Teknolojiler

| Katman | Teknoloji |
|--------|-----------|
| 👨‍💻 Dil | Kotlin |
| 🧩 UI | Jetpack Compose |
| ☁️ Backend | Firebase Auth & Firestore |
| 🖼 Profil Resmi | Base64 Encoding (Firebase Storage kullanmadan) |
| 🌐 Dil Yönetimi | `CompositionLocal` ve `stringResource()` |
| 🎨 Tema Yönetimi | Material3 + PreferencesManager |
| 🧠 Local Storage | SharedPreferences |
| 📍 Konum | İlçe bazlı filtreleme (Firestore'dan) |

---

## 🔧 Kurulum

1. Bu projeyi klonlayın:
   ```bash
   git clone https://github.com/kullaniciadi/Teamatch.git
   cd Teamatch
   ```

2. Android Studio ile açın (Arctic Fox veya daha yenisi önerilir)

3. Gerekli Firebase yapılandırmasını yap:
   - `google-services.json` dosyasını `app/` klasörüne ekleyin

4. Uygulamayı çalıştır:
   - Gerçek cihazda veya emülatörde başlat

---

## ✍️ Geliştirici Notları

- Profil fotoğrafları Firebase Storage yerine **Base64** olarak saklanmaktadır
- Davet sistemi oyuncular arasında birebir iletişim değil, **istek kutusu mantığı** ile çalışır
- Tema ve dil tercihleri, uygulama yeniden başlatıldığında korunur

---

## 📌 Yol Haritası

- [x] Kullanıcı oturum yönetimi
- [x] Maç oluşturma ve listeleme
- [x] Oyuncu profili yönetimi
- [x] Oyuncuya davet gönderme sistemi
- [x] İstek kutusu ekranı
- [x] Çoklu dil ve tema desteği
- [ ] Push Notification ile davet bildirimi *(yolda...)*
- [ ] Harita üzerinde saha listesi entegrasyonu *(planlanıyor)*

---

## 🤝 Katkı

Pull request’ler memnuniyetle karşılanır! Büyük değişiklikler için önce bir **issue** açman önerilir.

---

## 📬 İletişim

**Geliştirici:** Yusufcan Kurtulan

📧 Mail: yusufcan.kurtulan@gmail.com  
🔗 GitHub: [@yusufcankurtulan](https://github.com/yusufcankurtulan)
