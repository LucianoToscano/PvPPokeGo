from pathlib import Path
p=Path('app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleEngine.kt')
s=p.read_text()
s=s.replace('playerName?.let(repo::possibleFastMoves)?.size ?: 0 <= 1 -> EnergyConfidence.CONFIRMED','(playerName?.let(repo::possibleFastMoves)?.size ?: 0) <= 1 -> EnergyConfidence.CONFIRMED')
p.write_text(s)
