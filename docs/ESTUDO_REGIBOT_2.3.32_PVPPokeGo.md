# PvPPokeGo — Estudo técnico do APK autoral Regibot 2.3.32
**Data:** 2026-10-09. **Status:** estudo estático do arquivo fornecido pelo usuário; propostas para versões futuras. **Nenhum código Kotlin/Java/C++, serviço, APK ou build do PvPPokeGo alterado.** O usuário declarou ser o criador do Regibot e autorizou analisá-lo para aprender e aproveitar ideias.

## Arquivo realmente examinado
- Nome enviado: `Regibot_v2.3.32.apk`; origem: upload desta conversa, sem execução ou instalação.
- Tamanho: `52.937.403` bytes; SHA-256 **`6f08a94c19b7d84b42e412d51cbe5f583732606f3681bee5d7b323b344bdb6b7`**.
- ZIP Android íntegro para leitura (inspeção estática); **940 entradas**, DEX ~4,0 MiB, resources ~1,9 MiB, recursos assets descompactados ~102,5 MiB, library native ~0,54 MiB.
- Manifest Android binário decodificado sem execução: package `com.regitools.regibot`, versionName `2.3.32`, versionCode `45`, minSdk `26`, targetSdk `36`, application `com.regitools.regibot.RegiBotApp`, `allowBackup=false`, `usesCleartextTraffic=false`.
- Declaradas permissões INTERNET, NETWORK_STATE, localização aproximada, overlay, notificações, foreground service especial, WAKE_LOCK, receber boot, ignorar otimização bateria, escrita externa e solicitação de instalação; **não transplantar permissões ao PvPPokeGo** sem avaliação individual, consentimento e UX.
- `classes.dex` contém ~3.433 class_defs, incluindo Activities nomeadas `ConfigManagerActivity`, `RegiPlayScannerConfigActivity`, `SavedPokemonListsActivity`, `SavedFilterSetsActivity`, `RouteManagerActivity`, `WorldClockActivity`, `PokedexActivity`, `CooldownTrackerActivity`, `RaidsConfigActivity`, `WebhookConfigActivity`, `AccountManagerActivity`, além de `BotService`, `BootReceiver`, `RemoteKeepAliveService`. **Nomes de classes não provam o comportamento**, mas corroboram existência de interfaces/módulos.
- Arquivos `assets/regi_inject_pro`, `assets/regi_inject_17`, `assets/regi_run` e `lib/arm64-v8a/libregibot-security.so` têm assinatura ELF arm64. Foram apenas identificados por cabeçalho; **não executar, invocar ou reutilizar os injetores**, bem como `assets/regibot-bridges/*`, scripts de manipulação de processo ou hooks. Eles indicam arquitetura de automação/injeção com riscos de conta e compatibilidade; não são a estratégia do PvPPokeGo.
- `assets/regibot_watchdog.sh`, `assets/account_snap.sh`, `assets/attach_sampler.sh` identificados por arquivo; não foram executados, não foram publicados nem copiadas partes que possam tratar de contas/sessões.

## Os cinco módulos de configuração do Regibot
Arquivos JSON originais sob `assets/config_schema/`. Todos expõem `schemaVersion=1`, `mode`, `feature`, `title`, `comment` e `sections`. Estrutura de campos com chaves, tipo, valores padrão e, quando presente, destino de armazenamento. Dados exatos:
| Esquema | Seções | Campos totais | Campos não-nota | Exemplo de categorias |
|---|---:|---:|---:|---|
| `catchemall.json` | 15 | 119 | 106 | Captura/filtros, inventário, incubação, pesquisa, overlay |
| `grunt.json` | 11 | 95 | 79 | Composição de equipes, batalha, recuperação, filtros |
| `raid.json` | 10 | 71 | 66 | Reides, tier, time, lobby, participantes, Dinamax, cura |
| `regiplay.json` | 10 | 71 | 59 | Scanner Pokémon, reides, membros da Equipe GO Rocket, filtros de IV/gênero/tamanho, detalhes mostrados |
| `sniper.json` | 10 | 38 | 34 | Filtros, exclusões, espécies e status |
| **Total** | **56** | **394** | **344** | **Sem chaves duplicadas entre os campos do mesmo arquivo, verificado por script local** |

**Ponto mais reaproveitável:** configuração **orientada por esquema** de telas e modos; permitir ao usuário ativar campos, definir valores, usar listas e filtros, salvar perfis, exportar/importar e migrar `schemaVersion`. O PvPPokeGo deve implementar **sua própria estrutura** com types claros, validação, teste, versão e persistência segura, e o estado ON/OFF visível (ON colorido / OFF cinza como solicitado). Não copiar sem revisão a semântica de chaves que automatizam o jogo.

### Configurações aproveitáveis de forma legítima
- `scanner_pokemon` prevê seleção por espécie, filtros de IV, gênero, tamanho, Grande/Ultra Liga e quais campos exibir (IV, CP, nível, tempo). Adaptar ao **scanner visual por captura consentida** da tela do GO com campos de confiança e confirmação manual. Não importar nenhuma API de scanner remoto clandestino.
- `scanner_raids` prevê seleção por nível do chefe, tipo de reide, ginásio e horário: referência para **planejador de reides PvE** com histórico e calendário.
- `scanner_grunts` prevê filtros por tipo e líderes: referência para **enciclopédia de counters contra Rocket** em modo consulta, não auto-batalha.
- `raid.min_players...Tier/Mega/Shadow/Primal` contém limites configuráveis por categoria: referência de UX para **sugestão de grupo mínimo editável por usuário**, mas os números do APK **não devem ser considerados verdade universal**, já que força real depende da equipe, boss, buffs e regras atuais.
- `quick_scope` inclui opacidade e número de linhas: referência para **painel/overlay compacto e configurável** do PvPPokeGo.
- `bag_management` inclui limites do que manter: **não controlar o inventário do GO**; uma alternativa segura é checklist de preparo PvE e alertas baseados em dados digitados.
- `auto_switch`, `instant_catch`, `tap_teleport`, `sniper`, `auto_dodge`, ações de exploração via hooking/cliente e captura automática **NÃO são candidatos de transferência** para o produto PvPPokeGo visual, pela diferença de produto, permissões e riscos de regras/contas.

## Catálogos internos — NÃO importar cegamente
- `assets/pokemon.json` é lista com **1.250 registros e 1.025 IDs de espécie distintos**; IDs repetidos podem se referir a formas/variantes. Campos observados: `id`, `name`, `form`, `gen`, `type`, `imageUrl`. URLs de sprites referenciam PokeAPI, uma dependência/atribuição externa.
- **Armadilha importante:** `type` nesta lista tem valores como `normal` (937), `form` (225), `legendary` (65), `mythical` (23): é uma **classificação de registro**, NÃO o tipo elemental de combate. Por exemplo Bulbasaur, Altaria e Cramorant aparecem marcados `type:normal` nessa coluna. O motor matemático do PvPPokeGo jamais pode consumir essa coluna como tipagem em batalha.
- `assets/pokemon_forms.json` contém mapas `name_to_id` e `id_to_name`, cada um com **1.473** itens. Uso possível: **revisar cobertura de variantes** na nossa Pokedex e identificação OCR; **validar pares bidirecionais, inexistências e IDs** antes de usar. Não pressupor atualização oficial nem licenciamento dos nomes/ativos.
- Há Leaflet JS/CSS **1.9.4** em `assets/leaflet/`, biblioteca para mapa interativo. A licença do projeto é BSD-2-Clause, com condições de preservar aviso/licença: https://github.com/Leaflet/Leaflet/blob/main/LICENSE . Cartografia/tiles/localização possuem licenças e consentimento à parte. Nosso uso, se desejado, deve focar mapas de **eventos/reides locais voluntários** com precisão grossa e opt-in, sem mapa de spawns teletransportáveis.

## Arquitetura técnica e antirregressão que vale adaptar
1. **Schema-driven config**: modelo versionado `settings_schema_v1` com `id`, tipo, padrão, restrição, `enabled`, dependências, descrição PT-BR, separação por grupos PvP/Scanner/Overlay/Comunidade/PvE. Validação antes de gravar. Snapshot rollback se o restore falhar.
2. **Perfis e presets**: coleções salvas de filtros e times (referências classes `SavedFilterSetsActivity` e `SavedPokemonListsActivity`); aplicar perfil com confirmação, opção desfazer. Importar/exportar JSON documentado, com migrações.
3. **OCR contextual**: reusar a infraestrutura `TeamScreenshotScanner` do **PvPPokeGo** e adicionar regras por contexto (ficha, avaliar, equipe, reide, Rocket) com confiança por campo, imagem descartada após extração e revisão antes do cadastro. Treinamento/calibração de IV Avaliar segue **P0 não resolvido**.
4. **Planejamento PvE**: filtros de reide/chefes, número de jogadores, boosts Mega/Dinamax, time atual, comparador DPS/TDO, previsão de esforço e compartilhamento de plano — inteiramente **consultivo**; sem automação do combate.
5. **Mapas e fuso**: `WorldClockActivity` e Leaflet são referências para agenda inter-regional e horários de torneios; evitar capturar localização precisa por padrão.
6. **Telemetria/diagnóstico legítimo**: watchdog do **próprio PvPPokeGo**, exportação de logcat/logs do app com consentimento e mascaramento de dados, verificação de saúde do OCR/overlay e de importações; sem monitorar, modificar ou injetar no processo do Pokémon GO.
7. **Segurança**: solicitar estritamente as permissões exigidas; não reproduzir `REQUEST_INSTALL_PACKAGES`, instalação silenciosa, autoboot, `WAKE_LOCK` ou root/injeção como dependências. Controle de retenção, armazenamento criptografado de segredos se surgirem perfis sociais e exclusão de dados. O Regibot tem `CryptoKeyManager` e `NativeSecurity` no DEX, mas nomes de classes **não validam criptografia nem segurança**; preferir padrões Android auditáveis.
8. **Verificação de fontes**: validar licenças de código do autor versus bibliotecas de terceiros incorporadas, imagens, PokeAPI e dados; para reaproveitamento fiel de código autoral, preferir obter o repositório fonte original do Regibot, não converter de APK decompilado com obfuscação.

## Matriz para próximas versões PvPPokeGo
| Ideia | Prioridade | Situação em 0.5.34 | Próximo passo |
|---|---|---|---|
| Preferências dinâmicas versionadas ON/OFF e presets | P0 | Já há configurações/backup, mas não foi integrado modelo Regibot | Planejar migração sem perder configurações e posições HUD |
| Scanner por filtros de IV, liga, forma e gênero | P0 | Scanner de ficha implantado, **Avaliar IV precisa calibrar** | Confidence score, datasets e casos reais |
| Reposição/counters contra Rocket | P1 | Banco e análise de golpes existentes | Guia de tipos e time, sem automatizar |
| Preparador PvE para raid/Dinamax | P1 | Ideia/estudo, não implementado plenamente | Formas, boss, número de participantes, DPS/TDO |
| Perfis de equipes e filtros e export/restore | P1 | Histórico/backup existente | Presets independentes por liga e modo |
| Agenda de duelos/raid entre fusos/mapa voluntário | P1 | Comunidade somente planejada | Avisos, compartilhamento, localização aproximada |
| Watchdog/logcat sem segredos e painel de integridade | P1 | Diagnóstico Logcat prévio citado no projeto; verificar código em HEAD | Reaproveitar somente conceitos compatíveis |
| Injetores/hooking/teleporte/autocatch e bots de reide | **Não levar** | Não faz parte do escopo | Preservar arquitetura visual não intrusiva |

## Questões de segurança e conformidade
- O Centro de Ajuda de Pokémon GO identifica **falsificação de localização, software modificado/não oficial e acesso não autorizado ao cliente/backend** como violações das regras: https://scopelyexplore.helpshift.com/hc/pt-br/5-pokemon-go/faq/1307-violating-the-terms-of-service/ . Não implementar mecanismos que dependam dessas ações no PvPPokeGo.
- **Nenhum APK de terceiros ou binário nativo do Regibot está aprovado automaticamente para ser embutido**. Ainda que o usuário seja autor, existem dependências/assets de terceiros e diferenças técnicas entre arquiteturas.
- Não foram feitos pentest, validação dinâmica, conferência de tráfego, sandbox malicioso ou teste de login; **não alegar segurança ou estabilidade operacional do APK examinado**.
- Não publicar credenciais, URLs internas, material de licença, chaves, identificadores de conta, scripts de injeção ou dados de jogadores nos documentos/repositórios. Só registrar referências de alto nível, evidências e riscos.
- **Sem compilação, sem novo APK e sem alterações de funcionalidade** nesta etapa. Preservar versão 0.5.34 e trabalhos 0.5.35 já documentados.

## Continuidade
Antes de qualquer desenvolvimento: ler `docs/PROJECT_CONTINUITY_GUIDE.md`, conferir HEAD/branch/checklist e distinguir P0 em andamento das novas ideias. Integrar este estudo ao registro canônico e ao Drive sem mudar funcionalidades existentes. Propostas complementam `docs/ESTUDO_CALCY_POKERAID_GOHUB_0.5.35.md` e `docs/PROPOSTA_COMUNIDADE_TREINADORES_0.5.35.md`.
