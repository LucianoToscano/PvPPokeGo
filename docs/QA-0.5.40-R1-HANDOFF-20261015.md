# QA 0.5.40-R1 — handoff de continuação

Data: 15/10/2026 (UTC-03). Estado: fonte QA com alterações, **não compilada como APK nesta revisão**.

Fonte editável completa: https://drive.google.com/file/d/1QeLf0nYsjYxlAYgDVcxLO4MsVdfI4r_v/view?usp=drivesdk

SHA-256: `d20338b317ed8ac11c8ad7ccb34af71d1dabaede3823fe011856bdf28dd1e7fb`

No ZIP: hardening de EnemyEnergyTracker, FastMovePhaseTracker, EnemyChargedHistory; validador SHA offline e testes; workflow offline sem APK externo; changelog, relatório e patch. Pacote `com.lucianotoscano.pvppokego`, code 45, versão 0.5.40 mantida.

Verificação: 5 testes Python passaram; 1.751 Pokémon / 352 golpes / 1.167 sprites / 1.167 fingerprints; ZIP íntegro. **Cinco novos testes Kotlin ainda não executados**. Falta Android SDK/Gradle, build `standardDebug` e QA no aparelho.

Este branch é **documental**: mudanças de código completas estão no ZIP Drive e não devem ser consideradas sincronizadas neste branch. Consulte o Guia Mestre adicionado e o arquivo `docs/REGISTRO_QA_0.5.40-R1_2026-10-15.md` dentro do ZIP. Não promover release sem CI e teste em celular.
