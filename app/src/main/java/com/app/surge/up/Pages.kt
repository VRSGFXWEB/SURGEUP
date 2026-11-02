package com.app.surge.up

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

/** Kerangka halaman penuh: latar map, tombol kembali, judul miring merah. */
@Composable
fun PageFrame(title: String, onBack: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.menu_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x99000000), Color(0x55000000), Color(0xCC000000)))))
        content()
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(SlantShape(8f)).background(Color(0xCC000000)).border(1.dp, CRed, SlantShape(8f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                title, color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 20.sp,
                modifier = Modifier.clip(SlantShape(14f)).background(Brush.horizontalGradient(listOf(CRedDark, CRed)))
                    .padding(horizontal = 30.dp, vertical = 8.dp)
            )
        }
    }
}

// ---------------- KARAKTER ----------------
class CharInfo(val id: String, val name: String, val tag: String, val note: String)

private val CHARS = listOf(
    CharInfo("PUNK", "PUNK", "KARAKTER DASAR", "Karakter bawaan game. Dipakai di lobby dan di dalam pertandingan."),
    CharInfo("JOSEPH", "JOSEPH", "KARAKTER SPESIAL", "Tampil di lobby, profil, dan undangan. Di dalam pertandingan masih memakai Punk.")
)

@Composable
fun CharPage(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var sel by remember { mutableStateOf(Session.char) }
    var used by remember { mutableStateOf(Session.char) }
    val info = CHARS.firstOrNull { it.id == sel } ?: CHARS[0]
    PageFrame("KARAKTER", onBack) {
        Column(
            Modifier.align(Alignment.CenterStart).padding(start = 14.dp, top = 56.dp).width(170.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (c in CHARS) {
                SlotCard(c.name, if (used == c.id) "DIPAKAI" else "", sel == c.id) { sel = c.id }
            }
            for (i in 0 until 2) {
                Box(
                    Modifier.fillMaxWidth().height(64.dp).clip(SlantShape(12f)).background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = CSoft, modifier = Modifier.size(24.dp))
                }
            }
        }
        Box(Modifier.align(Alignment.Center).padding(bottom = 6.dp, end = 40.dp)) { CharArt(sel, 330) }
        Column(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(310.dp)
                .background(Brush.horizontalGradient(listOf(Color(0x00000000), Color(0xEE0A0A0E))))
                .padding(start = 36.dp, end = 18.dp, top = 64.dp, bottom = 18.dp)
        ) {
            Text(info.name, color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 34.sp)
            Box(Modifier.width(170.dp).height(3.dp).background(CRed))
            Text(info.tag, color = CRed, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            Text(info.note, color = CSoft, fontSize = 13.sp, modifier = Modifier.padding(top = 14.dp))
            Spacer(Modifier.weight(1f))
            RedButton(if (used == sel) "DIPAKAI" else "PAKAI", Modifier.fillMaxWidth(), enabled = used != sel) {
                Session.char = sel
                used = sel
                UiState.toast = info.name + " dipakai"
                scope.launch { Social.syncProfile() }
            }
        }
    }
}

@Composable
fun SlotCard(name: String, badge: String, on: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(64.dp).clip(SlantShape(12f))
            .background(if (on) Brush.horizontalGradient(listOf(CRed, CRedDark)) else Brush.horizontalGradient(listOf(Color(0xCC000000), Color(0x99000000))))
            .clickable { onClick() }.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 16.sp)
            if (badge.isNotEmpty()) Text(badge, color = CGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

// ---------------- SENJATA ----------------
class WpInfo(val id: String, val name: String, val type: String, val img: Int, val note: String)

private val WEAPONS = listOf(
    WpInfo("ak", "AK-47", "SENAPAN SERBU", R.drawable.wp_ak, "Senjata otomatis jarak menengah."),
    WpInfo("sg", "M1887", "SHOTGUN", R.drawable.wp_sg, "Tembakan sebar, kuat di jarak dekat.")
)

@Composable
fun WeaponPage(onBack: () -> Unit) {
    var sel by remember { mutableStateOf(Session.weapon) }
    var used by remember { mutableStateOf(Session.weapon) }
    val info = WEAPONS.firstOrNull { it.id == sel } ?: WEAPONS[0]
    PageFrame("SENJATA", onBack) {
        Column(
            Modifier.align(Alignment.CenterStart).padding(start = 14.dp, top = 56.dp).width(170.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (w in WEAPONS) {
                SlotCard(w.name, if (used == w.id) "DIPAKAI" else "", sel == w.id) { sel = w.id }
            }
        }
        Image(
            painter = painterResource(info.img),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.Center).padding(end = 40.dp, top = 20.dp).width(430.dp)
        )
        Column(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(310.dp)
                .background(Brush.horizontalGradient(listOf(Color(0x00000000), Color(0xEE0A0A0E))))
                .padding(start = 36.dp, end = 18.dp, top = 64.dp, bottom = 18.dp)
        ) {
            Text(info.name, color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 34.sp)
            Box(Modifier.width(170.dp).height(3.dp).background(CRed))
            Text(info.type, color = CRed, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            Text(info.note, color = CSoft, fontSize = 13.sp, modifier = Modifier.padding(top = 14.dp))
            Text("Senjata yang dipakai jadi senjata awal tiap ronde.", color = CSoft, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
            Spacer(Modifier.weight(1f))
            RedButton(if (used == sel) "DIPAKAI" else "PAKAI", Modifier.fillMaxWidth(), enabled = used != sel) {
                Session.weapon = sel
                used = sel
                UiState.toast = info.name + " dipakai"
            }
        }
    }
}

// ---------------- TEMAN ----------------
@Composable
fun FriendsPage(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var id by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val friends = UiState.friends
    val reqs = UiState.reqs
    val room = UiState.room
    PageFrame("TEMAN", onBack) {
        Row(
            Modifier.fillMaxSize().padding(top = 68.dp, start = 14.dp, end = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                Modifier.width(260.dp).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(CPanel)
                    .border(1.dp, CLine, RoundedCornerShape(8.dp)).padding(12.dp)
            ) {
                Text("TAMBAH TEMAN", color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 16.sp)
                Text("ID kamu: " + Session.id, color = CSoft, fontSize = 12.sp, modifier = Modifier.padding(vertical = 6.dp))
                Field(id, { if (it.length <= 10) id = it.filter { c -> c.isDigit() } }, "ID teman", Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                GoldSlantButton(if (busy) "..." else "KIRIM", Modifier.fillMaxWidth()) {
                    if (!busy) {
                        busy = true
                        scope.launch {
                            msg = Social.sendFriendReq(id)
                            busy = false
                        }
                    }
                }
                if (msg.isNotEmpty()) Text(msg, color = CGold, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Column(
                Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(CPanel)
                    .border(1.dp, CLine, RoundedCornerShape(8.dp)).padding(12.dp)
            ) {
                Row {
                    Text("DAFTAR TEMAN", color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text(friends.size.toString(), color = CGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                if (friends.isEmpty()) {
                    Text("Belum ada teman.", color = CSoft, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(friends, key = { it.id }) { f ->
                            val inRoom = room != null && room.members.any { it.id == f.id }
                            FriendRow(f, inRoom) {
                                scope.launch { UiState.toast = Social.inviteFriend(f) }
                            }
                        }
                    }
                }
            }
            Column(
                Modifier.width(260.dp).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(CPanel)
                    .border(1.dp, CLine, RoundedCornerShape(8.dp)).padding(12.dp)
            ) {
                Text("PERMINTAAN", color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 16.sp)
                if (reqs.isEmpty()) Text("Tidak ada permintaan.", color = CSoft, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    for (r in reqs) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(r.name, 34, nameColor(r.fromId))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                Text("ID " + r.fromId, color = CSoft, fontSize = 10.sp)
                            }
                            Text(
                                "TERIMA", color = Color(0xFF1A1205), fontWeight = FontWeight.Black, fontSize = 11.sp,
                                modifier = Modifier.clip(SlantShape(6f)).background(CGold).clickable {
                                    scope.launch {
                                        Social.acceptReq(r.fromId)
                                        UiState.reqs = UiState.reqs.filter { it.fromId != r.fromId }
                                        val f = Social.loadFriends()
                                        if (f != null) UiState.friends = f
                                    }
                                }.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF26262E)).clickable {
                                    scope.launch {
                                        Social.declineReq(r.fromId)
                                        UiState.reqs = UiState.reqs.filter { it.fromId != r.fromId }
                                    }
                                }.padding(6.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- PENGATURAN ----------------
@Composable
fun SettingsPage(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var gq by remember { mutableStateOf(Session.gq) }
    var gs by remember { mutableStateOf(Session.gs) }
    var gf by remember { mutableStateOf(Session.gf) }
    var music by remember { mutableStateOf(Session.music) }
    var vol by remember { mutableStateOf(Session.musicVol) }
    PageFrame("PENGATURAN", onBack) {
        Column(
            Modifier.align(Alignment.TopCenter).padding(top = 66.dp).width(560.dp).fillMaxHeight()
                .verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SetSection("GRAFIK") {
                Text("Kualitas grafik", color = Color.White, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                    for (o in listOf("RENDAH" to "low", "SEDANG" to "mid", "TINGGI" to "hd", "ULTRA" to "ultra")) {
                        Chip(o.first, gq == o.second) {
                            gq = o.second
                            Session.gq = o.second
                        }
                    }
                }
                SetToggle("Bayangan", gs) {
                    gs = it
                    Session.gs = it
                }
                SetToggle("Tampilkan FPS", gf) {
                    gf = it
                    Session.gf = it
                }
                Text("Grafik berlaku saat masuk pertandingan berikutnya.", color = CSoft, fontSize = 11.sp)
            }
            SetSection("SUARA") {
                SetToggle("Musik lobby", music) {
                    music = it
                    Session.music = it
                    if (it) Music.start(ctx) else Music.pause()
                }
                Text("Volume musik " + (vol * 100f).toInt().toString() + "%", color = Color.White, fontSize = 13.sp)
                Slider(
                    value = vol,
                    onValueChange = {
                        vol = it
                        Session.musicVol = it
                        Music.setVolume(it)
                    },
                    colors = SliderDefaults.colors(thumbColor = CRed, activeTrackColor = CRed, inactiveTrackColor = Color(0xFF2A2A32))
                )
            }
            SetSection("AKUN") {
                Text("Nama: " + Session.name, color = Color.White, fontSize = 13.sp)
                Text("ID: " + Session.id, color = Color.White, fontSize = 13.sp)
                Text("Surge Up Beta", color = CSoft, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SetSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(CPanel)
            .border(1.dp, CLine, RoundedCornerShape(8.dp)).padding(14.dp)
    ) {
        Text(title, color = CRed, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
fun SetToggle(label: String, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Chip("OFF", !on) { onChange(false) }
        Spacer(Modifier.width(6.dp))
        Chip("ON", on) { onChange(true) }
    }
}

@Composable
fun Chip(label: String, on: Boolean, onClick: () -> Unit) {
    Text(
        label, color = Color.White, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, fontSize = 12.sp,
        modifier = Modifier.clip(SlantShape(8f)).background(if (on) CRed else Color(0xFF1E1E24))
            .clickable { onClick() }.padding(horizontal = 18.dp, vertical = 7.dp)
    )
}

// ---------------- PROFIL PEMAIN LAIN ----------------
@Composable
fun OtherProfileDialog(f: Friend, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val clip = LocalClipboardManager.current
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Row(
            Modifier.width(540.dp).heightIn(max = 340.dp).clip(RoundedCornerShape(10.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF220A12), Color(0xFF0E0E13))))
                .border(1.dp, CRed, RoundedCornerShape(10.dp)).padding(14.dp)
        ) {
            Box(Modifier.width(170.dp).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) { CharArt(f.ch, 250) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(f.name, 64, nameColor(f.id), f.ava)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(f.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
                        Text("ID " + f.id, color = CSoft, fontSize = 12.sp)
                        Text(if (f.online) "Online" else "Offline", color = if (f.online) CGreen else CSoft, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Box(Modifier.clip(RoundedCornerShape(6.dp)).clickable { onClose() }.padding(6.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("BIO", color = CSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(if (f.bio.isEmpty()) "-" else f.bio, color = Color.White, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (f.id != Session.id) {
                        RedButton("UNDANG") {
                            scope.launch { UiState.toast = Social.inviteFriend(f) }
                            onClose()
                        }
                    }
                    RedButton("SALIN ID") {
                        clip.setText(AnnotatedString(f.id))
                        UiState.toast = "ID disalin."
                    }
                }
            }
        }
    }
}
