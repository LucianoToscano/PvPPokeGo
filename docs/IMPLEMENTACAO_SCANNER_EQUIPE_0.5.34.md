# PvPPokeGo 0.5.34 — implementação incremental do scanner de equipe

**Data:** 2026-10-09
**Branch:** `feature/team-scan-0.5.34-implementation` (derivada da branch de planejamento `feature/team-scan-0.5.34-planning`; preserva a última versão 0.5.33)
**Build público isolado de QA:** branch `preview/team-scan-0.5.34-20261009`, commit `1dc7a4ba2d9e073f53523392cbd4fb5356bbd8ea`, run https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37917670306 — **verificar resultado real antes de declarar APK pronto**.

## Alterações de código
- `data/TeamScanParser.kt`: parser sem estado de texto OCR da ficha, só aceita espécie encontrada no catálogo do app, PC com rótulo explícito PC/CP, PS com rótulo PS/HP, golpes compatíveis com espécies e nomes traduzidos. Não utiliza nickname "6/9/14" como IV/HP.
- `detect/TeamScreenshotScanner.kt`: acesso apenas via Android GetContent/Photo Picker iniciado pelo usuário; usa ML Kit existente e fecha o recognizer, não retém screenshot, não mexe no scanner/serviço de batalha. Duas opções: Ficha e Avaliar. Avaliar possui detector de evidência de barras deliberadamente conservador; **sem calibração de telas reais, não retorna IV numérico automaticamente, exibindo instrução de entrada manual**. É um trabalho ainda **incompleto**, não representar como scanner de IVs pronto.
- `ui/TeamBuilderPanel.kt`: controles independentes de importar ficha/Avaliar para cada slot 1/2/3, prévia de espécie/PC/PS/golpes, verificação de conflito de espécie, escolha de aplicar/descartar e gravação SOMENTE ao tocar “Salvar Pokémon”. Permite segunda captura da mesma vaga aplicando sobre campos não vazios. Histórico local dos Pokémon salvos após scan, com seleção para editar sem gravar automaticamente.
- `data/TeamPvpTools.kt`: fórmula independente de PC e ranking estatístico de IVs considerando 4096 combinações, CPM do `pvpoke_cpms.json` local e limites da liga (não representa “rank do meta”). Estima PC potencial nível 50, sem afirmar custo exato de doce/poeira. Laboratório simples de energia, quantidade de ágeis necessários, duração e DPE de golpes legais selecionados.
- `data/TeamScanHistoryRepository.kt`: histórico de até 30 Pokémon de scans confirmados, sem captura/URI/OCR bruto; deduplicado por espécie/PC/IVs/golpes. `SettingsRepository.kt` inclui `team_scan_history_v1` no backup portátil.
- `app/build.gradle.kts`: versionCode 39, versionName 0.5.34.
- Testes novos: `TeamScanParserTest.kt` e `TeamPvpToolsTest.kt` para identificação do Cramorant da imagem, rejeição de nickname/poeira como PS/PC, ataques ilegais, PC, Rank e energia.

## Limites honestos e pendências
- O botão Avaliar reconhece texto, porém **leitura de barras IV 0–15 não foi calibrada**, requer amostras reais da tela Avaliar, algoritmo geométrico robusto e testes por aparelho. Não anuncia IVs numéricos automaticamente.
- Captura ao vivo do Pokémon GO via MediaProjection continua para fase posterior; a implementação presente utiliza imagem selecionada pelo usuário.
- A conta de fortalecimento mostra PC potencial, **não os custos exatos** (tabela de poeira/doces e regras de nível/categoria precisam ser verificadas).
- Rankings: classificação estatística de IVs dentro do limite, não envolve resultados de PvPoke “meta” nem Elo; algoritmo presume nível até 51 e CPM local consistente. Seleção de formas deve ser conferida pelo usuário.
- OCR deve ser testado com múltiplas resoluções, idiomas, apelidos e screenshots da página de golpes; previsão de formas temporárias do Cramorant no overlay não alterada.
- Testes de unidade e build CI estão pendentes de conclusão do run acima; QA no Samsung do usuário não executado até receber feedback. Não copiar/embutir APK do Poke Genie.

## Regras de segurança e regressão
- Overlay, HUD, ícones, posições, motor de batalha, reservas e atualizações prévias 0.5.33 mantidos fora do patch principal.
- Correções de reconhecimento de batalha aliada continuam intactas. Backups/old `manual_team_v1` retrocompatíveis.
- Nenhuma imagem capturada é gravada: o conteúdo é processado em memória após seleção explícita.
- O GitHub privado é canônico; espelho público isolado recebe só arquivos de build necessários, sem tokens/chaves/históricos/dados de usuários.

## Verificações antes do próximo passo
1. CI público `:app:testStandardDebugUnitTest` e `:app:assembleStandardDebug`, conferir logs.
2. Corrigir todos os erros reais primeiro na branch privada; sincronizar blobs ao mirror; reexecutar CI.
3. Validar checksum e assinatura antes de publicar link do APK.
4. Pedir capturas reais de “Avaliar” e testar reconhecimento por barras no Samsung.
5. Passar as conclusões ao Guia Mestre e Google Drive, atualizando checklist com etapas feitas e pendentes.
