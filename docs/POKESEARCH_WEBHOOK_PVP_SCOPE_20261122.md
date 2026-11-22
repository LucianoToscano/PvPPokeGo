# PvPPokeGo — fronteira entre dados PvP e coordenadas de spawns (22/11/2026)

Status: registro de compatibilidade e prioridades derivadas das pesquisas de APIs/PGSharp/webhooks; NÃO cria radar, Sniper ou mudanças em combate.

## Arquitetura canônica preservada

PvPPokeGo é assistente visual EXTERNO de PvP, com OCR, três inimigos, energia, estimativas de dano, previsão e conselhos. O mestre docs/SINGLE_THREAD_MASTER_CONTEXT_2026-09-26.md deixa expressamente fora do escopo mapa/feed geográfico, spoofing, GPX, farm e VGP. Não misturar responsabilidades.

Fontes para cálculos: PvPoke e Game Master de espécies/golpes; somente dados de PvP autorizados e provenientes de versões rastreáveis. GoBattleSim é referência de simulação a avaliar sob GPL-3.0 (não incorporar código por cópia direta). PoracleNG, ReactMap e bots de ranks fornecem exemplos de FILTROS e não controlam batalha do cliente Android.

## Pesquisa de coordenadas — integração apenas futura e opcional

O PokeSearch contém proposta de normalizador puro para eventos de aparições de scanners autorizados:
https://github.com/LucianoToscano/PokeSearch/tree/feat/authorized-spawn-webhook-normalizer-20261122

Eventuais spawns são observações temporárias com sourceId, species, IV/CP/nível, latitude/longitude e despawn; dados não equivalem a uma detecção de batalha, nem validam movesets, HP, energia ou posição do adversário em PvP. Nenhuma coordenação automática de teleporte, webhook ou conta deverá entrar no core.

Para futura identificação de ranks PvP, exigir IV completo (ataque, defesa, stamina), nível, forma e teto da liga. Uma simples porcentagem IV=100 não é rank 1 automático. Ausências têm de permanecer desconhecidas. Separar rank calculado offline de coordenadas observadas.

## Bibliografia técnica

- Catálogo com fontes, links e status: https://docs.google.com/document/d/12-I-rUfnQR0YL_mDExTJfx2zXgfxgB8-Bkp2tyuVPfI/edit
- Romain Thomas, PGSharp Ekoparty 2021: https://drive.google.com/file/d/1nEf72fUU4hXS2V8alnuc4pv3unUdINV3/view
- GitHub do estudo: https://github.com/romainthomas/pgsharp
- ReactMap: https://github.com/WatWowMap/ReactMap
- PoracleNG: https://github.com/jfberry/PoracleNG
- GoBattleSim: https://github.com/biowpn/GoBattleSim
- Automate Flows: https://github.com/chrisisth/PokemonGo-Automate-Flows

A palestra de 2021 é sobre JNI/Lua/GPS do PGSharp, NÃO sobre inteligência de escudo/counter nem API mundial de Pokémon. Não replicar bypass, credenciais ou hooks como se fossem atuais.

## Consequências para a implementação

- Nenhuma mudança nos algoritmos de energia, escudos, Charged Move, OCR, contagem de fast moves ou overlays deste PR.
- Não declarar "PvP inteligente implementado" apenas com referência a um simulador ou a um webhook.
- Mudanças futuras devem ter branch própria, licença compatível, testes, homologação e avaliação de confiança de observações.

Base da branch: main f58154df441303e66bb468550edc945db97f04c5.
