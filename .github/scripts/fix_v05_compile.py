from pathlib import Path

# BattleEngine precedence cleanup.
p=Path('app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleEngine.kt')
s=p.read_text()
s=s.replace('playerName?.let(repo::possibleFastMoves)?.size ?: 0 <= 1 -> EnergyConfidence.CONFIRMED','(playerName?.let(repo::possibleFastMoves)?.size ?: 0) <= 1 -> EnergyConfidence.CONFIRMED')
p.write_text(s)

# API 36 declares getMediaProjection as nullable; keep a local non-null reference.
p=Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt')
s=p.read_text()
old='''                val pm = getSystemService(MediaProjectionManager::class.java)\n                projection = pm.getMediaProjection(resultCode, resultData).also { mp ->\n                    mp.registerCallback(object : MediaProjection.Callback() {\n                        override fun onStop() { stopSelf() }\n                    }, null)\n                }\n\n                capture = ScreenCapture(this@BattleOverlayService, projection!!).also { it.start() }\n'''
new='''                val pm = getSystemService(MediaProjectionManager::class.java)\n                val mediaProjection = pm.getMediaProjection(resultCode, resultData)\n                    ?: error("MediaProjection indisponível")\n                mediaProjection.registerCallback(object : MediaProjection.Callback() {\n                    override fun onStop() { stopSelf() }\n                }, null)\n                projection = mediaProjection\n\n                capture = ScreenCapture(this@BattleOverlayService, mediaProjection).also { it.start() }\n'''
if old not in s:
    raise SystemExit('MediaProjection marker not found')
s=s.replace(old,new,1)
p.write_text(s)
