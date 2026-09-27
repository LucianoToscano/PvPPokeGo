from pathlib import Path
p=Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt')
s=p.read_text()
s=s.replace('''                                    e.onBattleConfidence(presenceTracker.confidence)
                                    markBattleEvidence(now)
''','''                                    e?.onBattleConfidence(presenceTracker.confidence)
                                    markBattleEvidence(now)
''',1)
s=s.replace('''                                val changes = e.onDetection(d)
                                if (changes.playerChanged || changes.enemyChanged) frameDetector?.reset()
                                if (changes.playerChanged) switchChoicePromptUntilMs = 0L
                                val textEvent = e.onBattleText(d.battleText, now)
                                when {
                                    textEvent.newChargedPrompt -> frameDetector?.freezeForCharged(now)
                                    textEvent.chargedMoveConfirmed -> frameDetector?.freezeForCharged(now, 2_200L)
                                }
''','''                                val changes = e?.onDetection(d)
                                if (changes?.playerChanged == true || changes?.enemyChanged == true) frameDetector?.reset()
                                if (changes?.playerChanged == true) switchChoicePromptUntilMs = 0L
                                val textEvent = e?.onBattleText(d.battleText, now)
                                if (textEvent != null) when {
                                    textEvent.newChargedPrompt -> frameDetector?.freezeForCharged(now)
                                    textEvent.chargedMoveConfirmed -> frameDetector?.freezeForCharged(now, 2_200L)
                                }
''',1)
p.write_text(s)
