
[Dokümantasyon için](./pd_rapor1.docx) 

# Real-Time Syntax Highlighter

Bu proje, Java ve Swing kullanılarak geliştirilmiş basit bir gerçek zamanlı sözdizimi vurgulayıcıdır. Kullanıcı metin girdikçe, kodun temel yapı taşlarını (anahtar kelimeler, tanımlayıcılar, sayılar, string'ler, yorumlar vb.) tanır ve bunları farklı renklerle vurgular.

## Özellikler

*   **Gerçek Zamanlı Vurgulama**: Yazarken anında sözdizimi renklendirmesi.
*   **Temel Dil Desteği**: C benzeri/Java benzeri bir sözdizimini destekler (anahtar kelimeler, tanımlayıcılar, sayılar, string'ler, operatörler, ayırıcılar, yorumlar).
*   **Dosya İşlemleri**: Metin dosyalarını açma ve kaydetme.
*   **Satır Numaraları**: Kod editöründe satır numaralarının gösterimi.
*   **Sözdizimi Kontrolü**: "Parse Et" butonu ile temel bir sözdizimi geçerlilik kontrolü (Bu özellik `Parser.java` tarafından sağlanır).
*   **Renk Efsanesi**: Kullanılan token türlerini ve karşılık gelen renkleri gösteren bir bölüm.
*   **Desteklenen Token Türleri ve Renkleri**:
    *   `KEYWORD`: Mavi
    *   `IDENTIFIER`: Yeşil
    *   `NUMBER`: Turuncu
    *   `OPERATOR`: Kırmızı
    *   `SEPARATOR`: Macenta
    *   `STRING`: Pembe/Mor
    *   `COMMENT`: Gri
    *   `UNKNOWN`: Gri

## Teknolojiler

*   **Java**: Ana programlama dili.
*   **Swing**: Kullanıcı arayüzü (GUI) için kullanılmıştır.

## Proje Yapısı

Proje, aşağıdaki ana paketlerden oluşmaktadır:

*   **`src/lexer`**:
    *   `Lexer.java`: Kaynak kodu metnini analiz ederek `Token` nesnelerinden oluşan bir listeye dönüştürür.
*   **`src/model`**:
    *   `Token.java`: Bir token'ı (tür, değer, başlangıç ve bitiş pozisyonu) temsil eden veri sınıfı.
    *   `TokenType.java`: `KEYWORD`, `IDENTIFIER`, `NUMBER` gibi farklı token türlerini tanımlayan enum.
*   **`src/parser`**:
    *   `Parser.java`: `Lexer` tarafından üretilen token dizisini alarak basit bir sözdizimi analizi yapar. (Bu dosyanın içeriği detaylı incelenmemiştir, ancak GUI'deki "Parse Et" butonu bu sınıfı kullanır.)
*   **`src/ui`**:
    *   `SyntaxHighlighterGUI.java`: Swing bileşenlerini kullanarak metin editörünü, menüleri, butonları ve diğer arayüz elemanlarını oluşturur. Gerçek zamanlı vurgulama mantığını ve dosya işlemlerini yönetir.

## Nasıl Çalışır?

1.  **Kullanıcı Girdisi**: Kullanıcı metin editörüne kod yazar.
2.  **Gecikmeli Tetikleme**: Her karakter değişikliğinde, kısa bir gecikmenin ardından vurgulama işlemi tetiklenir.
3.  **Lexical Analiz (Tokenization)**: `Lexer`, metin içeriğini baştan sona tarar ve `Token`'lara ayırır. Her token, türüne (örneğin, anahtar kelime, sayı, operatör) ve metindeki konumuna göre sınıflandırılır.
4.  **Stil Uygulama**: `SyntaxHighlighterGUI`, elde edilen token listesini kullanarak `JTextPane` üzerindeki metne stiller (renkler) uygular. Her `TokenType` için önceden tanımlanmış bir renk kullanılır.
5.  **Sözdizimi Kontrolü (İsteğe Bağlı)**: Kullanıcı "Parse Et" butonuna tıkladığında, `Lexer` önce metni token'lara ayırır, ardından bu token'lar `Parser`'a gönderilir. `Parser` basit bir sözdizimi kontrolü yaparak sonucunu arayüzde gösterir.

## Nasıl Çalıştırılır?

Proje bir Java Swing uygulamasıdır.
1.  Projeyi bir Java IDE'sine (Eclipse, IntelliJ IDEA vb.) aktarın.
2.  `src/ui/SyntaxHighlighterGUI.java` dosyasındaki `main` metodunu çalıştırın.

Bu, gerçek zamanlı sözdizimi vurgulayıcı uygulamasını başlatacaktır.
