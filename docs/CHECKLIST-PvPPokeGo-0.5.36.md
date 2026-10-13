# Checklist PvPPokeGo 0.5.36 — 13/10/2026

**Status:** build QA/debug verificado, entregue na nova pasta Google Drive. **Ainda não aprovado em aparelho real.**
**Branch:** `feature/battle-correspondence-0.5.36-20261009`.
**HEAD base auditado:** `8b32a050390e8b0475ca3d1579c7759424c54480` (antes desta atualização documental).
**Referências:** [Guia Mestre](PROJECT_CONTINUITY_GUIDE.md), [links oficiais](PROJECT_LINKS.md), [implementação de correspondência](IMPLEMENTACAO_CORRESPONDENCIA_BATALHAS_0.5.36.md).
**Checklist anterior:** [0.5.29](CHECKLIST-PvPPokeGo-0.5.29.md); [80 requisitos originais](CHECKLIST-PvPPokeGo-0.5.26.md). Não apagar tarefas históricas.

## Build e entrega — concluído com evidência
- [x] Identificada versão com código compilável mais recente: `0.5.36`, `versionCode=41`.
- [x] Localizada CI Standard pública: run `37992092526`, SUCCESS; pacote alternativo sem downloads de sprites: run `37992150961`, SUCCESS.
- [x] Recuperado artefato `11645731162` do build offline, não APK renomeado.
- [x] Conferido SHA-256 real do APK `e1ff7e2ac9e0a5f6e940f1d334bb2c7722409d7018bc3781253652a0679c1a94`.
- [x] ZIP do artefato íntegro; APK íntegro; assets `gamemaster.json`, `pokemon_icon_atlas.png/json`, `pokemon_visual_fingerprints.json` presentes.
- [x] Assinatura v2=true, 1 signatário QA/debug, conforme relatório incluído no artefato.
- [x] APK e ZIP enviados ao novo Drive `1ybogVRRnaOe0KtWjlovxS56bJOi_JewI`; IDs e tamanhos retornados e validados.
- [x] Checklists, guia de links e ponto de entrada de novo chat atualizados nesta branch.
- [ ] Assinatura permanente de release; não confundir APK QA com release final.

## Motor, time, reserva e continuidade
- [x] Código 0.5.36 inclui `BattleCorrespondenceTracker` com confirmação em dois frames e proteção contra falso início após OCR espúrio.
- [x] `ReserveCardMatcher`: distinção por espécie/forma/PC e conflitos não confirmados por eliminação.
- [x] `BattleHistorySegmentMatcher`: junção conservadora de trechos apenas com evidência, sem reescrever histórico ao ler.
- [x] Regressão de construtor `BattleHistoryEvent` nos testes Kotlin corrigida no código que originou o build.
- [x] `testStandardDebugUnitTest` e `assembleStandardDebug` passaram nos workflows públicos definitivos (evidência CI).
- [ ] Validar com vídeo/captura de aparelho: reconhecer 3 Pokémon antes da batalha, inclusive formas regionais e PC repetido.
- [ ] Validar trocas simultâneas, reservas 2/3, retorno do ativo, desmaio, início no meio da batalha e continuação após captura perdida.
- [ ] Validar que imagem do card/ícone nunca desaparece ao mostrar `ANALISANDO`, respeitando atlas e retrato estável.
- [ ] Confirmar separação de partidas e histórico de vitórias/derrotas, eventos e tempo de ataques em QA real.
- [ ] Revisar integridade de escudos, barra de HP, cálculo de dano e energia exibidos no HUD.
- [ ] Conferir launcher/ícone oficial no Samsung e backup/restauração das posições.
- [ ] Verificar consumo/performance no reconhecimento contínuo e evidências registradas em modo diagnóstico.

## Outros recursos preservados ou pendentes
- [ ] Confirmar por regressão em aparelho: menu, olho, cadeado, controles de arrasto, seleção de ataques, exportação de configurações e histórico.
- [ ] Scanner Avaliar IV 0–15 calibrado e integrações comunitárias (reides, trocas, clãs): backlog, não implementados neste build de correspondência.
- [ ] QA Android16/Samsung com registros mascarados de dados pessoais, sem captura de outros aplicativos.
- [ ] Decidir promoção para build de release somente após evidência física, assinatura de distribuição e checklist concluído.

## Links da entrega
- Pasta Drive NOVA: https://drive.google.com/drive/folders/1ybogVRRnaOe0KtWjlovxS56bJOi_JewI
- APK DIRETO: https://drive.google.com/file/d/14wzZDCx0RMCFtH-olGYlFb9FDnpaATHu/view?usp=drivesdk
- ZIP COM SHA E ASSINATURA: https://drive.google.com/file/d/1Y_ngMw12VcEPKMvBWVynMHnp6WQTyFOa/view?usp=drivesdk
- Workflow: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37992150961

**Registro em 13/10/2026:** nenhuma alteração do código de produção nesta atualização de Drive; APK já compilado na run definitiva em 09/10, recuperado, revalidado e republicado na nova pasta.
