# PvPPokeGo 0.5.28 — Checklist de restauração visual

A referência da aparência é a versão 0.5.17, documentada no [Guia Mestre](PROJECT_CONTINUITY_GUIDE.md). Manter o checklist integral da 0.5.27 e a engine mais recente sem rollback.

- [x] APK 0.5.17 recebido, estrutura ZIP verificada.
- [x] Comparar código de HUD da branch `release/0.5.17` com `release/0.5.27`.
- [x] Restaurar `drawReserves` com indicadores pequenos, tipos e labels como em 0.5.17.
- [x] Restaurar indicadores da escolha de troca de 0.5.17, com fallback quando OCR não confirma nome.
- [x] Restaurar visual do triângulo de ataques e energia simples; prever detalhes avançados só em debug.
- [x] Preservar desenho original do cadeado, olho, tipos e coordenadas padrão, verificados idênticos no código.
- [x] Preservar o ícone oficial transparente enviado, sem voltar à arte da 0.5.17.
- [x] Incrementar versão para 0.5.28 (code 33).
- [x] Atualizar espelho público de build 0.5.28 sem publicar histórico privado.
- [x] Confirmar testes e compilação Standard 0.5.28 PASS no Actions (run 37716045867).
- [x] Conferir integridade, assinatura debug v2 e SHA-256 do APK.
- [x] Entregar APK instalável e registrar link.
- [ ] QA real em Samsung comparado com o HUD da 0.5.17: reservas, olho, cadeado, tipos, ataques, escolha de troca, posições e logo.
- [ ] Release assinado permanentemente (não confundir com QA debug).

## Entrega confirmada — Standard QA 0.5.28

- Primeira tentativa `37715432893` falhou em Kotlin (`stableBattleAdvice` removido por engano no recorte visual). Corrigido no privado: `f1de2771f5a0e88a840d8864134ea5424cdd8d5a`.
- Build final público `37716045867`: **SUCCESS** — https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37716045867
- Commit do espelho compilado: `491106a928efd810c9351c8e356d3513ad03c6f2`.
- Testes `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, integridade ZIP, atlas/Game Master/launcher e `apksigner verify`: PASS.
- APK `PvPPokeGo-0.5.28-QA-debug.apk`, **79.193.451 bytes**.
- SHA-256: `504643fe67f08ab599f35d74b8cf3cf1f7f079324badae9024712d59c54c359d`; comparação exata com `SHA256SUMS.txt` do workflow.
- APK Signature Scheme **v2: true**, um signer (QA/debug, NÃO certificado permanente).
- Logo transparente oficial dentro do APK possui hash `f8d4f45922e6af0022e3eaad1d255fedd2b790650f542e62315042f8bd1dbf51`, idêntico ao arquivo original fornecido.
- ZIP de distribuição + checksum no Drive: https://drive.google.com/file/d/1sPMZZMsKpWukN0J3yxhBYVGYeJHu4RYe/view?usp=drivesdk
- QA no Samsung do visual 0.5.17 restaurado **ainda pendente**. Compilação não garante aparência idêntica sob dados/posições reais. Backup de configurações antes de substituir instalação com assinatura diferente.
