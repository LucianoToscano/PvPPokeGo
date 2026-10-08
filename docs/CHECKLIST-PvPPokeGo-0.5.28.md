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
- [ ] Confirmar tests/compile Standard 0.5.28 PASS no Actions (run 37715432893).
- [ ] Conferir integridade, assinatura debug v2 e SHA-256 do APK.
- [ ] Entregar APK instalável e registrar link.
- [ ] QA real em Samsung comparado com o HUD da 0.5.17: reservas, olho, cadeado, tipos, ataques, escolha de troca, posições e logo.
- [ ] Release assinado permanentemente (não confundir com QA debug).
