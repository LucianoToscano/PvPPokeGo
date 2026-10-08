# PvPPokeGo — Checklist 0.5.27 (continuação sem reiniciar)

A lista integral 1–80 e PBE permanece em [CHECKLIST-PvPPokeGo-0.5.26.md](CHECKLIST-PvPPokeGo-0.5.26.md). Nenhum item foi removido ou marcado como resolvido sem QA.

- [x] Nova branch privada `release/0.5.27` derivada de 0.5.26.
- [x] Compactar visual das duas reservas com texto/PC/status/HP e tipos menores.
- [x] Tirar halo/círculo decorativo grande dos retratos no HUD.
- [x] Manter reserva visível se mapeamento em troca for incerto.
- [x] Empacotar PNG oficial transparente fornecido pelo usuário sem arte substituta.
- [x] Corrigir fundo do adaptive launcher e ajustar inset.
- [x] Incrementar versionName para 0.5.27 e versionCode 32.
- [x] Build Standard e testes unitários 0.5.27 **PASS**.
- [x] Validar APK assinado (QA/debug v2), assets, SHA-256 e entregar download.
- [ ] Testar visualmente o launcher Samsung e reservas/controle/ícones durante troca no aparelho.
- [ ] Release com assinatura permanente (não confundir com QA debug).

Compilação pública: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37712804512

## APK Standard QA 0.5.27 — evidência real

- [x] Run público GitHub Actions `37712804512` concluído SUCCESS: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37712804512
- [x] Testes `:app:testStandardDebugUnitTest` e compilação `:app:assembleStandardDebug`, verificação de integridade ZIP e assinatura debug v2.
- [x] APK real `PvPPokeGo-0.5.27-QA-debug.apk`, tamanho 79.193.451 bytes.
- [x] SHA-256 `cbf336cddfa31bf0639df41491b4e1cc0019b3bc4403a6c394a3bfa5966d3025`, idêntico ao checksum do workflow.
- [x] Ícone PNG oficial dentro do APK: 1.453.830 bytes, SHA-256 `f8d4f45922e6af0022e3eaad1d255fedd2b790650f542e62315042f8bd1dbf51`, idêntico ao PNG enviado pelo usuário.
- [x] Atlas Pokémon, Game Master e launcher empacotados.
- [x] Cópia ZIP da entrega no Google Drive oficial: https://drive.google.com/file/d/1KIvivodofc3QA72oS0zeUTJPplFUkUb3/view?usp=drivesdk
- [ ] QA Samsung: layout de reservas, continuidade/troca, botões/ícones preservados e arte do launcher realmente exibida pelo sistema.
- [ ] Assinatura permanente e migração sem reinstalação; QA/debug pode ter certificado diferente de APKs anteriores.
