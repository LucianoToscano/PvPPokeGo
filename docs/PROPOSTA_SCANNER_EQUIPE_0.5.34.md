# PvPPokeGo 0.5.34 — PROPOSTA Scanner da Equipe 1/2/3 + estudo Poke Genie
**Data:** 2026-10-09
**Status:** especificação funcional registrada, NÃO IMPLEMENTADA, sem build/APK novo.
**Base canônica preservada:** `feature/diagnostics-hp-ui-0.5.33`, APK QA 0.5.33, código legado do overlay e os controles/posições preservados.
**Evidência do usuário:** duas capturas da ficha do Cramorant e arquivo de referência `Poke Genie -Remote Raid IV PvP v8.19.0_(416) (Premium) (Fixed) (...)-TekMods.com.apk`. **Esclarecimento posterior de 2026-10-09:** o usuário afirma que adicionou pessoalmente "Premium" ao nome apenas por estética e que o aplicativo é uma versão normal. O nome do arquivo, isoladamente, NÃO é prova de modificação, origem oficial, autenticidade ou segurança. Podemos inspecionar e estudar o APK fornecido como referência técnica, sem executá-lo no ambiente. Não assumir direitos de redistribuição de código, bibliotecas proprietárias, assets/arte/traduções/datasets de terceiros apenas pelo nome ou pela permissão do usuário para analisar.

## Pedido literal
“Alguma forma de por para scanear como no poke genie e sanear o pokemon 1, 2 e 3.. e ele ja preencher todos os Campos .. E algo mais que possamos aproveitar no apk do genie.”

## O que JÁ existe e deve ser reaproveitado
- `ui/TeamBuilderPanel.kt`: painel SUA EQUIPE com 3 vagas e seletor de liga. Edita e salva `speciesId/speciesName, cp, maxHp, IV atk/def/hp, gender, mega, shadow, fastMoveId, chargedMove1Id, chargedMove2Id`.
- `data/TeamSetupRepository.kt` e `TeamSetupModels.kt`: salva `manual_team_v1`, `team_recognition_mode_v1`, política `ManualTeamPolicy.sanitize/validateSlot`. Modo Manual pré-carrega lista válida, mas NÃO presume qual Pokémon está ativo, e o modo Automático permanece independente.
- `detect/BattleOcrDetector.kt`: **ML Kit TextRecognition Latin** já instalado e usado em quadros do Pokémon GO via `MediaProjection`; `PokemonVisualRecognizer` auxilia espécie/forma durante batalha. NÃO duplicar bibliotecas OCR só por analogia com Genie.
- `GameMaster` de `assets/gamemaster.json`, nomes PT-BR em `assets/move_names_ptbr.json`, `MoveDef` e `PokemonDef`: validadores para espécie, movimentos compatíveis e nomes.
- `SettingsRepository` exporta configuração/posições/time atual. Nova interface deve manter ícones, cores ON/OFF, HUD, histórico, arena e scan de batalha existentes.

## Fluxo recomendado: Scanner de Equipe independente do OCR de batalha
1. Seletor de vaga **Pokémon 1 / Pokémon 2 / Pokémon 3**, mostra progresso “0/3, 1/3...” e marcador de dados confirmados.
2. Ação “Escanear ficha”: **fase 1** importar screenshot via seletor Android (alternativamente capturar 1 frame pela MediaProjection consentida, se sessão válida, na fase 2). Reconhecer a região com imagem/PC/nome/PS/tipos e golpes. Quando a ficha rolada exige segunda screenshot, permitir **adicionar captura da MESMA vaga** e fundir somente campos compatíveis.
3. Ação “Escanear avaliação (IVs)”: reconhecer a tela de avaliação com barras/indicadores 0–15 de ataque, defesa e PS, exigindo calibração/dupla confirmação nos limites. Não extrair IV de apelido como dado confirmado; “Cramorant⑥⑨⑭” pode ser apenas renomeação e deve vir como sugestão não confiável.
4. Pré-visualização/editável com fonte e confiança **confirmado pelo jogador / OCR provável / indeterminado** por campo; nunca inventar IVs/sexo/nível/golpes/PC ou rank. Em conflito entre imagens, mostrar “revisar” e preservar cadastro anterior. Botão “Aplicar nesta vaga” separado de “Salvar equipe”.
5. Quando 3 slots válidos, mostrar **“Usar esta equipe agora”**. Solicitar confirmação do usuário antes de mudar `TeamRecognitionMode.MANUAL` e sincronizar time com o motor; NÃO sobrescrever automaticamente a seleção do modo Automático nem reatribuir o Pokémon ativo/reservas somente por ordem de cadastro.
6. Salvar o `manual_team_v1` pelo repositório existente, sem exigir login, permissão de storage ampla ou armazenamento permanente das capturas. Se usuário quiser, exportar resultado por configuração/backup atual, sem incluir screenshots por padrão.
7. Auto-scan estilo iVision (**fase 3 opcional**): só com consentimento de captura Android existente; ativar quando reconhecer ESTAVELMENTE a tela de ficha/avaliação; debounce, hash visual/ROI, redução de uso de CPU/bateria, não capturar indevidamente outras telas/apps, não interagir ou simular toques no Pokémon GO; botão flutuante de confirmação sem cobrir as zonas de toque nativas.

## Campos e confiabilidade a partir das imagens atuais
Exemplo *visual* da ficha de Cramorant, **não dados previamente confirmados no celular**:
- Espécie apresentada “Cramorant”, PC 1485, PS 126/126, tipo Voador/Água, símbolo feminino, golpes: Bicada (dano 6 na captura), Mergulho (energia 50), Voar (energia 80); não assumir que números exibidos são sempre os valores atuais do Game Master, validar versão local.
- O texto de apelido exibido junto ao nome parece 6/9/14 e NÃO representa medição automática de IVs; pedir captura do painel “Avaliar”, ou confirmação manual.
- O painel de **Forma Engolidora/Devoradora** descreve mudança transitória **durante combate** associada ao HP depois de Surfar/Mergulho e efeitos quando golpe carregado atinge; **não** são duas espécies/slots permanentes. Modelar estado de batalha/moves condicionais somente depois de conferir dados de jogo atuais e revalidar com Game Master (Cramorant/Gulp Missile).
- Captura contém poeira/doces/altura/peso/data: são informações opcionais, não bloqueiam equipa PvP; não expor dados pessoais por padrão.

## Informações “inspiradas” no Genie (sem copiar implementação)
**Inspeção estática de cabeçalhos ZIP do arquivo fornecido:**
- Presença de recursos como `res/layout/home_scan_cell.xml`, `scan_details_layout.xml`, `ivision_overlay.xml`, `dialog_input_appraisal.xml`, `activity_pvp_iv_settings.xml`, `activity_move_details.xml`, `battle_team_overlay.xml`, `activity_history_filter.xml`.
- Presença de dados de OCR Tesseract `assets/tessdata/eng/`, `jpn_v4/`, `kor_v5/`, `number/`, `tw_v6/` e de `libtesseract.so`; isto apenas evidencia componentes empacotados, **não prova quais são executados ou sua qualidade**.
- Presença de nomes de tabelas `assets/plists/...pvp.../move.../shadow.../cpm...`, mas **NENHUM dado bruto é redistribuído ou importado**. Preferir fonte aberta versionada e licenciada apropriadamente (PvPoke Game Master + algoritmos próprios).

Funcionalidades para desenvolver com implementação própria e fonte compatível:
1. Scanner de ficha e avaliação, nível aproximado pela curva de PC quando há evidências, mostrar **intervalo** até confirmar IV/nível.
2. Rank de IV PvP por espécie/forma/limite da liga (otimização Stat Product, empates e Best Buddy, XML/CPF validados), sem confundir “rank IV” com “meta rank”, Elo ou Arena.
3. Análise de golpes compatíveis, energia, cooldown e DPE com mecânica PvP separada de PvE; move pool proveniente da versão local dos dados.
4. Calculadora de fortalecimento/evolução (PC e custos usando dados locais). Não prometer exatidão para eventos/buffs sem versão.
5. Histórico de escaneamentos local + detectar duplicatas sem apagar históricos; exportar JSON/CSV, opt-in se guardar capturas.
6. Rótulos de apelidos PvP gerados pelo app, sempre com cópia manual para o jogo, sem injeção de texto/toques.
7. Comparador de time 3×3, tipo/coverage/switch e alertas de sinergia associados aos modelos offline já propostos.
8. Mudanças de forma temporárias em combate, ilustradas pelo Cramorant, separadas do catálogo de formas permanentes.

## Arquitetura, escopo e implementação por fases
**Fase A** — scanner a partir de screenshots do usuário para cada vaga, com parser puro testável (`TeamScreenshotParser`), adaptador ML Kit (`TeamScreenshotOcr`) e seletor Android em `TeamBuilderPanel`. Leitura de formulário completo pode requerer duas imagens. Merge explícito após revisão, testes JUnit para extração e conflitos.
**Fase B** — IV appraisal via barras e segmentação de imagem com calibração/resoluções/tema; camada de confiança e snapshots opcionais; testes instrumentados reais.
**Fase C** — captura direta pelo overlay usando `MediaProjection` existente ou `AccessibilityScreenCapture` apenas quando permitido e em sessão consentida; gatilho de tela de ficha/avaliação, não disputar pipeline do motor de batalha.
**Fase D** — classificação IV, custos, histórico, rename e integração no laboratório de equipe.

## Portas de qualidade antes de liberar APK novo
- Não modificar original 0.5.33/QA/debug ou positions HUD. Criar branch funcional separada, manter snapshots e docs canônicas.
- Casos de teste: screenshot Cramorant 1485/126, duas imagens mesma vaga, troca de Pokémon entre imagens, duas espécies com nomes semelhantes, app em PT-BR/EN, escudo/HP não misturados com o cadastro, IV ausente, appraisal incompleta, Mega/Sombroso, ligas 1500/2500/sem limite, duplicados, captura sem consentimento, cache e memória de Bitmap.
- Rodar unit/integration/compile Standard; quando APK pedido, usar **espelho público existente** `LucianoToscano/PvPPokeGo-APK-Build` com SHA de fonte preservada e apenas snapshot sanitizado (NUNCA espelhar o APK de referência de terceiro sem licença/verificação de direitos).
- QA em Samsung do usuário, validar telas/posição do overlay. Nada nesta proposta foi implementado, compilado ou instalado.

## Segurança e respeito a direitos
- **Correção solicitada pelo usuário:** "Premium" no nome foi inserido por ele mesmo; não se deve inferir que o APK é um mod/crack apenas por "Premium"/"Fixed" no nome. A procedência e a assinatura original não foram autenticadas nesta análise estática. É permitido usar o arquivo enviado para pesquisar estrutura/recursos e extrair requisitos de funcionalidades. Não instalar/executar ou redistribuir esse APK em nossos builds sem necessidade e sem confirmação de direitos; não copiar ativos/código de terceiros nem presumir autorização de sublicença.
- Não extrair ou reutilizar classes, recursos visuais, textos e bancos de dados de terceiros como se fossem nossos. Reproduzir apenas **ideias e comportamentos gerais**, com algoritmos próprios e dados com licença compatível.


### 2026-10-09 — Retificação de procedência/nome do arquivo
O usuário esclareceu: “Fui eu quem renomeei para Premium... pode usar ela sim... Mas é uma versão normal”. Portanto, a classificação anterior de “APK modificado não oficial” com base apenas no filename foi **indevida** e foi retirada. O arquivo passa a ser tratado como **APK enviado pelo usuário, de procedência não verificada tecnicamente, autorizado pelo usuário para análise e referência**. Nenhuma conclusão sobre assinatura oficial é implicada. Prosseguiremos com inspeção estática, comparação funcional e futura implementação própria do scanner 1/2/3, sem alterar o app de batalha até autorização específica de implementação.
