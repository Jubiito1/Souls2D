<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="prueba" tilewidth="32" tileheight="32" spacing="1" margin="1" tilecount="4" columns="2">
 <image source="New Piskel.png" width="67" height="67"/>
 <tile id="0">
  <objectgroup>
   <object id="1" type="plataforma" x="0" y="0" width="32" height="32"/>
  </objectgroup>
 </tile>
 <tile id="1">
  <objectgroup draworder="index" id="2">
   <object id="2" type="plataforma" x="0" y="0" width="32" height="32"/>
  </objectgroup>
 </tile>
 <tile id="2">
  <objectgroup draworder="index" id="2">
   <object id="1" type="rampa" x="0.363636" y="31.4545">
    <polygon points="-0.454545,0.545455 31.5455,-31.4545 31.5455,0.545455"/>
   </object>
  </objectgroup>
 </tile>
</tileset>
