# Project Context Memory — PvPPokeGo

Atualizado em: 2026-09-26

## Objetivo

O PvPPokeGo é o projeto de assistência de batalha PvP. Ele deve concentrar recursos de leitura visual, overlay, contagem de energia/golpes, análise de matchup, identificação de moveset, suporte a escudos e recomendações em batalha.

## Fontes analisadas

Foram usados como referência funcional e arquitetural os arquivos fornecidos pelo proprietário do projeto:

- `PVPPokeGo_Source_Phase1.zip`
- `PVPPokeGo_Source_Phase1(1).zip` (duplicado idêntico)
- `Pokemod_13.3.4.apk`
- `ipogo-237_1790099437.apk`
- `Poke Genie -Remote Raid IV PvP v8.15.0 B404 (Premium) ... .apk`
- `Poke Genie ... (1).apk` (duplicado idêntico)
- `Pokémon GO_0.429.1(1).apk`

## Descobertas que interessam ao PvPPokeGo

### Poke Genie
Arquitetura especialmente relevante para o PvPPokeGo:

- overlay Android;
- captura de tela/MediaProjection;
- OCR por regiões;
- identificação e análise visual;
- módulos equivalentes a `IVisionHelper` e `OcrHelper`;
- `BattleTeamOverlayView`;
- simulador/counter engine;
- cálculo de PvP IV;
- banco de movesets e efetividade de tipos;
- fallback por screenshot.

Essas ideias devem ser implementadas com código próprio no PvPPokeGo.

### iPogo / Pokémon GO 0.429.1
A comparação com o APK oficial mostrou funções concentradas em código nativo. Foram identificados nomes/símbolos associados a recursos como:

- shiny scanner;
- skip animations;
- skip Team Leader;
- skip Team Rocket;
- fast map;
- freeze Pokémon;
- spin/throw helpers;
- localização nativa;
- Virtual GO Plus/autoconnect.

Para o PvPPokeGo, apenas conceitos relacionados a batalha/análise visual devem ser considerados. Funções de spoofing/localização, automação invasiva ou bypass não fazem parte do escopo do PvPPokeGo.

### Pokemod
Foram observados componentes ligados a root/injeção, incluindo injector, busybox/sqlite e bibliotecas nativas de concealment. Isso serve apenas como referência de arquitetura externa e de funcionalidades. O PvPPokeGo não deve depender de injeção, concealment ou bypass.

## Funções prioritárias no PvPPokeGo

1. OCR/visão por ROI para identificar Pokémon e elementos da batalha.
2. Overlay modular e reposicionável.
3. Contagem de Fast Moves e energia por Pokémon inimigo.
4. Memória independente dos três Pokémon adversários.
5. Identificação/seleção dos Charged Moves possíveis.
6. Quantidade de Fast Moves restante para cada Charged Move.
7. Recomendação de shield baseada em ameaça, tipo, energia e HP.
8. Recomendação do melhor Charged Move entre os dois botões.
9. Identificação de matchup, fraquezas, resistências e tipos favoráveis.
10. Simulador/counter engine e integração com dados PvPoke locais.
11. Modo assist-only como padrão seguro; qualquer executor de gesto deve permanecer desacoplado da lógica central.
12. Logs/debug de reconhecimento para melhorar precisão.

## Regra de implementação

- Reimplementar comportamento e UX com código próprio.
- Não copiar bytecode, DEX, SO nativa ou código proprietário dos APKs de terceiros.
- Não remover licenças/premium de terceiros.
- Não incorporar concealment, anti-detection ou bypass de anti-cheat.
- Manter o core de batalha separado de Android/OCR/gestos para ser testável.

## Relação com o projeto de recuperação

O PvPPokeGo e o projeto de recuperação estilo PGSharp podem compartilhar componentes próprios de domínio, por exemplo:

- banco de Pokémon/tipos/moves;
- PvP IV;
- efetividade de tipos;
- battle energy tracker;
- reconhecimento visual genérico;
- logging/diagnóstico;
- modelos de configuração.

O código específico de localização, mapa, Virtual GO Plus e outras funções do projeto de recuperação não deve contaminar o core do PvPPokeGo.
