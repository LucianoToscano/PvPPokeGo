# Changelog

## 0.5.11

- Backup/restauração em JSON das configurações, escala, opacidade e posições personalizadas do HUD e menus.
- Importação retorna o layout travado por segurança e não restaura flags transitórias do serviço.
- Histórico local das últimas 50 batalhas com Pokémon, PC, liga, duração e linha do tempo.
- Registro dos ataques rápidos e carregados detectados dos dois lados com tempo relativo ao início.
- Tela para atualizar, expandir detalhes e limpar o histórico.
- Ícones e posições aprovados da base 0.5.10 preservados.
- Convenção oficial: PvPPokeGo-0.5.10.apk, PvPPokeGo-0.5.11.apk, PvPPokeGo-0.5.12.apk e assim por diante.

## 0.3.0-v03-overlay-counter

- Charged Moves reposicionados ~30 px para cima na referência 864x1536, aproximando-os do Fast Move.
- Círculos dos golpes aumentados e geometria desacoplada do antigo multiplicador 0.8.
- Indicador do Pokémon atual movido para fora do card original.
- Topo mostra até quatro tipos fortes, seta e os tipos do oponente.
- Ícones de tipo procedurais em estilo visual, sem usar assets do Pokémon GO.
- `BattleFrameEventDetector`: Fast Move detectado primariamente pela queda da barra de HP, com debounce baseado na duração do golpe.
- Fallback por pico de movimento apenas quando a barra de HP não está disponível.
- OCR de prompt de Charged Move e reset automático, preservando energia residual quando conhecida.
- Memória da equipe do jogador via OCR da tela de equipe e aprendizado por Pokémon ativo.
- Indicadores e tipos das reservas ancorados aos cards laterais.
- Debug mostra fonte/confiança do detector, HP, movimento, energia e equipe aprendida.
