# PvPPokeGo 0.5.29 — Recuperar reconhecimento dos aliados

Referência funcional dos aliados: [código 0.5.17](https://github.com/LucianoToscano/PvPPokeGo/tree/release/0.5.17). Referência visual do HUD: 0.5.17. Base de funções atual: 0.5.28.

- [x] Encontrada regressão de OCR nome/PC quando visual não tem match.
- [x] Recuperado fallback de nome + PC sem depender do atlas.
- [x] Recuperada leitura ampla 0.5.17 quando os recortes individuais não identificam os três Pokémon.
- [x] Exigidos três pares reconhecidos distintos no fallback amplo, sem inventar correspondências por coluna.
- [x] `needsOwnTeamScan` continua até 3 espécies/PCs confiáveis.
- [x] Cadência OCR de seleção ajustada para 1550 ms.
- [x] Testes unitários para reconhecimento OCR nome+PC, baixa confiança e dados ausentes.
- [x] Build Standard isolado configurado para 0.5.29.
- [ ] Build 0.5.29 com testes de regressão concluídos.
- [ ] APK validado/assinado e SHA256 registrado.
- [ ] Teste em aparelho Samsung: os 3 Pokémon são encontrados ANTES de entrar na batalha.
- [ ] Teste em aparelho Samsung: reservas seguem corretamente após troca, sem misturar PC/espécie.
- [ ] Revisar log de diagnóstico caso algum dos três não seja reconhecido.

Build público: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37719025048
