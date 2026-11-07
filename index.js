const mineflayer = require('mineflayer');
const express = require('express');

// Render'ın uyku moduna almaması için mini web sunucusu
const app = express();
const PORT = process.env.PORT || 3000;

app.get('/', (req, res) => {
  res.send('Minecraft AFK Bot 7/24 aktif çalışıyor!');
});

app.listen(PORT, () => {
  console.log(`Web sunucusu ${PORT} portunda dinlemede.`);
});

// Minecraft Bot Ayarları
const bot = mineflayer.createBot({
  host: 'severname.play.hosting', // Sunucu IP adresin
  port: 25565,                   // Sunucu portu
  username: 'AFK_Bot'            // Botun oyundaki ismi
});

const sifre = '12345678';        // Sunucu şifren

bot.on('spawn', () => {
  console.log('Bot oyuna başarıyla bağlandı!');
  
  setTimeout(() => {
    console.log('Kayıt komutu gönderiliyor...');
    bot.chat(`/register ${sifre} ${sifre}`);
    
    setTimeout(() => {
      console.log('Giriş yapılıyor...');
      bot.chat(`/login ${sifre}`);
    }, 3000);
    
  }, 2000);
});

bot.on('end', (reason) => {
  console.log(`Bağlantı kesildi (${reason}). 10 saniye sonra tekrar bağlanılıyor...`);
  setTimeout(() => {
    process.exit(1);
  }, 10000);
});

bot.on('error', (err) => {
  console.log('Bir hata oluştu:', err);
});