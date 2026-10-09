# PvPPokeGo — Estudo estático do APK Poke Genie (08–09/10/2026)
**Estado:** pesquisa/documentação. **Sem mudanças no código Android e sem APK novo.**
**Fonte primária:** APK enviado pelo usuário, Poke Genie `8.19.0`, versão interna `416`, pacote `com.cjin.pokegenie.standard`. O usuário esclareceu que RENOMEOU ele mesmo o arquivo com a palavra “Premium” apenas por estética; não inferir adulteração a partir do nome. SHA-256 do arquivo: `0c668336c534361aaad76021f9ab52b2bd9bd625df3b0ff83f524ab7f5eb347d`.

## Limite de evidência
Análise **estática** de ZIP/manifesto compilado, nomes dos recursos (XML binário), atividades/serviços declarados, bibliotecas nativas e organização dos datasets. **Não executado, não instalado e não validado funcionalmente.** Nome de layout/atividade = evidência de componente empacotado; não prova que o recurso está ativo, nem a qualidade do algoritmo.

O arquivo externo tem 1.814 entradas ZIP, dez `classes*.dex`, recursos de UI e 16 bibliotecas nativas por arquitetura; contém também `assets/yrf.apk` (~84 MB) com segundo APK ZIP usando mesmo package/version. Essa camada externa é um fato técnico da amostra e **não prova malware ou modificação intencional**; autenticidade e assinatura oficial não foram verificadas. O usuário autorizou o estudo, não há autorização/licença demonstrada para copiar/redistribuir código/datasets ou arte proprietária.

## Inventário fundamentado
| Área | Evidências efetivamente observadas no APK | Aplicação possível no PvPPokeGo |
|---|---|---|
| Captura/Scanner | `home_scan_cell`, `scan_details_layout`, `ivision_overlay`, `ScanDetailsActivity`; manifesto declara `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Scanner de fichas de Pokémon e de equipe de três vagas; OCR com revisão |
| OCR | `libtesseract.so`, `libleptonica.so`; `assets/tessdata/eng`, `jpn_v4`, `kor_v5`, `number`, `tw_v6` | Avaliar *benchmark* de OCR de números, mantendo o ML Kit que já temos |
| IV/Avaliação | `dialog_input_appraisal`, `iv_results_item`, `iv_comb_layout`, `PvPIvChartActivity`, `PvpCandidateActivity`, `activity_pvp_iv_settings` | Leitura de barras de Avaliar e cálculo independente de classificação de IV por liga |
| Golpes/PvP | `move_list.txt`, `move_pvp_data.txt`, `move_stats.txt`, `moveset_ranking_genie_v3.txt`, `MoveDetailsActivity` | Sugestões de moveset, energia, turnos, DPE e classificação de golpes |
| PC/fortalecimento | `cpm.plist`, `stardust.plist`, `evolve_data.txt`, `evolution_special.txt`, `gender_ratio.csv` | Simulação PC/custo de evolução e fortalecimento com **dados próprios versionados** |
| Tipo, Sombroso, Mega | `typing2.plist`, `shadow.csv`, `mega_moves.txt`, `TypeEffectivenessActivity`, `MegeEvolActivity` | Tipagem, resistências e variações de forma |
| Histórico/filtro | `HistoryActivity`, `MergeHistoryActivity`, `history_activity`, `merge_history`, `activity_history_filter` | Histórico local de Pokémon escaneados e comparação sem duplicações |
| Nomes organizados | `RenameActivity`, `rename_template_name`, `rename_pvp_iv_modal` e outras telas de nomeação | Expandir gerador de nomes próprios (apenas copiar apelido) |
| Simulador | `BattleSimulator.SimulationBestCounterActivity`, `BreakpointActivity` e layouts específicos | Laboratório PvP e orientação matchup/energia/escudo, validado contra motor próprio |
| Raid online/loja | `RaidLobbyActivity`, `RaidHistoryActivity`, `RaidShopActivity`, permissão Billing | **Baixa prioridade**: operação online/lojas não pertence ao scanner PvP |

**Observação:** os arquivos em `assets/plists` são texto real com listas/valores. Não copiar tabelas internas ao repositório. Os valores podem estar defasados em relação ao PvP atual e os direitos de redistribuição não foram validados.

## Comparação com a versão canônica do PvPPokeGo
- **Já existe:** `ui/TeamBuilderPanel.kt` com 3 slots e campos de espécie, forma, PC, PS, IVs, gênero, Mega/Sombroso e golpes. `TeamSetupModels.kt` (`ManualTeamPokemon`, `ManualTeamRoster`, `ManualTeamPolicy`) e `TeamSetupRepository.kt` persistem `manual_team_v1` e modo `AUTOMATIC/MANUAL`, sem alterar diretamente o motor.
- **Já existe:** `detect/BattleOcrDetector.kt` usando **ML Kit Latin** e ROIs para batalha/seleção de time; `PokemonVisualRecognizer.kt` auxilia espécie/forma; `TrainerTools.kt` já gera apelidos básicos. Game Master e nomes PT-BR são recursos do app.
- **NÃO existe pronto:** parser dedicado da ficha do Pokémon/da tela Avaliar para preencher 1/2/3, com aquisição de screenshot, confiança por campo e mesclagem de duas capturas. O OCR de batalha não deve ser reutilizado diretamente com ROIs errados.
- **Faltam por verificar/implementar:** engine de ranking IV por liga, leitura das três barras 0–15, cálculo CP/custo e histórico separado de escaneamentos. Não declarar estes elementos entregues sem código/testes.

## Plano P0: Scanner de equipe 1/2/3 — máximo retorno
1. `SUA EQUIPE` → tocar vaga **1**, **2** ou **3** → `Escanear ficha`.
2. Primeira versão: importar screenshot via **Android Photo Picker**. Analisar nome e espécie, PC, PS, sexo, tipos, movimentos. Permitir segunda screenshot da mesma vaga (campos fora da tela), com confirmação de identidade antes de mesclar.
3. `Escanear Avaliar` separado para ler graficamente as barras IV 0–15; nickname renomeado (`6/9/14`) é pista, **não validação**.
4. Mostrar prévia **confirmado/provável/indeterminado** campo a campo; só salvar na vaga após revisão. Campos não vistos ficam nulos, e uma nova leitura duvidosa não apaga valores já confirmados.
5. Alimentar `TeamSetupRepository` e edição atual sem migrar dados destrutivamente; o botão `Usar esta equipe agora` muda o modo para MANUAL só com confirmação explícita. A primeira vaga não implica Pokémon ativo na batalha.
6. Exemplo das capturas enviadas pelo usuário: **Cramorant, PC 1485, PS 126/126, Voador/Água, feminino, Bicada, Mergulho e Voar**. As formas Engolidora/Devoradora são estados transitórios em batalha — não duplicar registro de espécie.

### P1 e P2
- **P1:** scanner em tempo real a partir de `MediaProjection` já autorizada, detector de contexto `ficha/avaliação/batalha`, sem tocar no jogo nem prejudicar engine; classificação de IV/stat product da liga; recomendações de ataques (rápido, carregados, energia/turnos, DPE, tipos, buffs); comparação 3×3.
- **P2:** histórico de escaneamentos sem duplicatas, custo de fortalecimento/evolução, apelidos especializados/copiar, consultas de tipos/forma, filtros e exportação opcional.
- **Adiar:** sistemas de Raid remoto, shops, monetização, anúncios e dependências extras. Tesseract só seria cogitado depois de testes comparativos comprovando benefício vs ML Kit.

## Arquitetura proposta
`Photo Picker / MediaProjection consentida → ScreenContextClassifier → TeamScreenshotOcr → TeamScreenshotParser + IVBarAnalyzer → GameMaster Validator → FieldConfidence/Merge → Revisar e salvar → TeamSetupRepository → Equipe atual`.

Regras: preservar HUD e positions, reconhecimento de aliados/reservas, Logcat e HP 0.5.33, auto/manual, backup/restore (formato retrocompatível), performance; evitar conflito de OCR e captura. Testar screenshot Cramorant, duas capturas, apelido com IV falso, avaliação incompleta, slot errado, troca de Pokémon, Mega/Sombroso, rotação, CP acima da liga e dado desconhecido. Sem capturar texto bruto desnecessário nos logs.

## Status de entrega
**Estudo concluído e prioridades propostas. Nenhuma implementação Kotlin ou APK 0.5.34 produzido.** Continuar na branch `feature/team-scan-0.5.34-planning`; próxima implementação só em branch funcional isolada. Para eventual build, reutilizar `LucianoToscano/PvPPokeGo-APK-Build` e verificar checksum/assinatura e QA no dispositivo.
