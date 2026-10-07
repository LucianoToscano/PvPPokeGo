# PvPPokeGo 0.5.23

Assistente visual externo para batalhas PvP do Pokémon GO.

O aplicativo usa `MediaProjection` para analisar pixels visíveis da tela e `TYPE_APPLICATION_OVERLAY` para exibir informações. Não modifica Pokémon GO, não injeta código, não acessa memória do jogo e não automatiza cliques/toques.

A base visual V03 e todas as correções posteriores continuam preservadas.

## Principais recursos

- OCR de Pokémon, PC e texto de batalha, combinado com reconhecimento visual offline por espécie/forma;
- banco PvPoke offline para tipos, movepools, rankings e dados de batalha;
- contagem de Fast Moves por eventos visuais reais, com gates contra dupla contagem;
- energia individual por Pokémon, preservada durante trocas;
- identificação dos dois Charged Moves inimigos e seleção manual protegida contra sobrescrita;
- reservas com matcher por PC/forma/imagem/eliminação, HP estimado e matchup;
- histórico cronológico, checkpoints, exportação JSON/CSV/TXT/ZIP e diagnóstico;
- gravação interna MP4 e variante Recorder Compat separada para gravadores externos;
- backup/restauração das configurações e posições do HUD;
- modo batalha assistido com recomendações de shield, troca, energia e uso do melhor Charged disponível.

## Predictor 0.5.23

A 0.5.23 adiciona previsão antecipada de energia e Charged Moves.

`EnemyEnergyTracker` mantém hipóteses matemáticas quando o Fast Move ainda não está confirmado. `ChargedMovePredictor` calcula, para todos os Charged Moves legais:

- energia min/provável/max;
- Fast Moves e turns restantes;
- janela mais cedo e provável;
- READY possível/certo e armazenamento para dois Chargeds;
- EPT, DPT e DPE;
- dano estimado sem escudo, risco de KO/farm-down, bait e pressão de escudo.

O HUD mostra no máximo as ameaças mais urgentes e pode exibir uma faixa fantasma sobre o HP antes do Charged, sem cobrir a interface crítica do jogo.

A antecedência do alerta usa latência de captura/análise medida no aparelho (p95) + orçamento explícito de render, reação humana e margem de segurança. Um turn PvP é tratado como 500 ms.

Quando não há certeza, o app mantém intervalos/hipóteses e usa `READY?`, faixas e confidence em vez de inventar precisão.

Detalhes matemáticos e fontes: `docs/PREDICTIVE_ENGINE_0.5.23.md`.

## Dados e privacidade

O snapshot PvPoke é fixado em um commit e o build registra hashes SHA-256 dos datasets para auditoria/reprodução.

O aprendizado de padrões da batalha/histórico é local. A 0.5.23 não envia automaticamente o histórico do usuário a servidor de IA nem agrega dados entre usuários.

## Reservas

Os cards laterais da batalha nem sempre mostram identidade textual suficiente. O PvPPokeGo memoriza a equipe na tela pré-batalha, combina OCR/PC/imagem, reaprende quando o Pokémon entra em campo e só libera recomendação específica quando a identidade está confirmada.

## Créditos

Desenvolvimento PvPPokGo by: LucianoToscano  
(84) 99131-9021
