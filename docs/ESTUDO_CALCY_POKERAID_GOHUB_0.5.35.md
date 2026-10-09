# Estudo técnico: Calcy IV 3.44, PokeRaid 0.48.16 e Pokémon GO Hub DB
**Data:** 2026-10-09 · **Status:** análise estática e backlog; não implementado no APK 0.5.34 e sem APK novo.
**Método:** leitura dos dois XAPK enviados nesta conversa como ZIP, leitura de seus `manifest.json`, inspeção da lista de arquivos do APK base (incluindo nomes de layouts, modelos e ativos) e metadados da estrutura da base SQLite do Calcy; **sem executar, instalar, descompilar lógica protegida, transmitir dados dos usuários, extrair assets para o app ou testar serviços externos**. Navegação pública ao endereço indicado `https://db.pokemongohub.net/pt` e páginas exemplares. A presença de um recurso não demonstra funcionamento interno nem expõe protocolo/API.

## 1. Inventário de APKs anexados — evidência local
| Campo | Calcy IV | PokeRaid |
|---|---|---|
| Nome recebido | `Calcy IV_3.44.xapk` | `PokeRaid_0.48.16.xapk` |
| SHA256 **do arquivo XAPK** | `fe98aa69446630bf526326bdc723de67db678f2e90055aff6e30235586dabeb5` | `2dced3bd0a7400acd2cfa7e4b228c8af58c630144f4e01f99f084050d23e241c` |
| package (XAPK) | `tesmath.calcy` | `me.pokeraid` |
| versionName / code (XAPK) | `3.44 / 344110` | `0.48.16 / 165` |
| SDK mínimo/alvo declarados pelo **manifest.json do XAPK** | 28 / 35 | 32 / 36 |
| Entradas | APK base + splits arm64_v8a e xxhdpi | APK base + splits arm64_v8a, pt e xxhdpi |
| Arquivo ZIP válido | Sim, `testzip()` retornou sem erros | Sim, `testzip()` retornou sem erros |
| Base APK | `tesmath.calcy.apk` (~25,8 MB) | `me.pokeraid.apk` (~87,2 MB) |

**Permissões declaradas no manifest externo (não validadas contra o manifesto Android binário):** ambos listam `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE_MEDIA_PROJECTION`, `INTERNET`, `POST_NOTIFICATIONS`; PokeRaid também `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`. Isto não prova que captures ou envio a servidores ocorram nem que haja segurança/risco: avaliar permissões apenas após auditoria de fluxo e consentimento. Não há análise dinâmica ou de malware.

### 1A. Calcy IV — evidências observadas
- Há arquivos Tesseract `assets/tessdata/*.traineddata` e layouts `output_scan.xml`, `fragment_welcome_scan_mode.xml`, `dialog_new_appraisal.xml`, `item_appraisal_overlay.xml`: indicam capacidade/arquitetura de OCR + avaliação e de apresentação em overlay, mas não revelam algoritmo confiável nem licença de reutilização.
- Layouts `dialog_pvp_iv.xml`, `item_calcydex_pvp_iv.xml`, `fragment_pvp_meta.xml`, `dialog_csv_pvp_ranks.xml`, `dialog_search_string.xml`, `fragment_type_matchups.xml`, `dialog_power_up_preview2.xml`, `item_spinner_power_up_overlay.xml`, `output_pvp_trainer_overlay.xml`: referências para UX de IV, ranking de liga, filtros/buscas, dano/tipos, previsão de fortalecimento e comparação.
- Existe SQLite local `assets/gamestats/gamestats.db` (~913 kB). Foram inspecionados **somente os nomes das tabelas e colunas**, sem copiar linhas para o PvPPokeGo. Tabelas presentes: `Monsters`, `BaseMonsters`, `Moves`, `MonsterMoves`, `MonsterEliteTmMoves`, `MonsterLegacyMoves`, `PvpMoveBuff`, `Evolutions`, `ShadowSettings`, `TemporaryEvolutions`, `TemporaryEvolutionTypes`, `Localizations`, entre outras. Campos observados incluem atributos base, tipos, golpes PvP/PvE, chances de efeito, traduções BR, custo de evolução e purificação. **Não importar esta base proprietária/embutida**; usar dados oficiais ou fontes com licença auditada, e comparar conceitualmente o nosso modelo de dados.
- Melhor aprendizado arquitetural: **separar captura, reconhecimento, confiança, confirmação do usuário e cálculo**, com persistência apenas de dados estruturados. Já temos scanner de ficha, `TeamPvpTools`, `TeamScanHistoryRepository`, dados PvPoke offline e modo Auto/Manual. Prioridade ainda pendente: ler graficamente barras IV reais com calibração por dispositivo e evitar falsas certezas.

### 1B. PokeRaid — evidências observadas
- Layouts `fragment_community_tab.xml`, `fragment_friendship_tab.xml`, `fragment_explore_clans.xml`, `fragment_create_clan_details.xml`, `fragment_clan_channel.xml`, `fragment_trade_offer_list.xml`, `fragment_trader_list.xml`: na UX mostrada pelo usuário, comunidade com amizade, clãs, troca, armazenamento, lista de desejos e ofertas. **Não copiar diretamente layout, marca, imagens ou lógica.**
- Layouts `fragment_raid_room.xml`, `fragment_create_raid_room.xml`, `fragment_raid_coordinator_dashboard.xml`, `fragment_raid_room_members.xml`, `fragment_rate_host.xml`, `fragment_raid_recognition.xml`, `fragment_active_raids_filter.xml`: referências para sala de encontro, participante/anfitrião, filtros, relatórios e avaliações; recurso real depende de serviços e usuários conectados.
- Layouts `fragment_pvp_matcher_create_room.xml`, `fragment_pvp_matcher_filter.xml`, `fragment_pvp_history.xml`, `fragment_pvp_assistant.xml`, `fragment_pvp_matcher_onboarding_add_friendship_code.xml`: referências de convite PvP/pareamento/histórico com nickname, código e liga.
- Layouts `fragment_smart_iv_compose.xml`, `fragment_notification_settings.xml` e vídeos `vid_smart_iv_onboarding.mp4`, `vid_pvp_matcher_onboarding_*.mp4`: evidência de onboarding IV e de fluxos de notificação; `assets/mlkit-google-ocr-models/` indica presença de recursos para OCR. **Não prova que processamento é totalmente local**.
- Tela de trocas observada: species, CP, golpes, flags especiais, região ampla, data, ofertas/lista de desejos. Para PvPPokeGo usar **lista de intenções/coordenação**, não uma transferência de Pokémon direta entre jogadores. Remover localizações precisas por padrão e tratar Pokémon/CP como informação declarada até validação.
- Potencial MVP **offline**: agenda de duelo/reide, listas de desejos e ofertas exportáveis por Share Sheet, salvar cartões voluntários. Features de verdadeiras salas online/clãs/chat/pareamento, inscrições, push, avaliações e moderação precisam de backend e governança; **não presentes na build atual**.

## 2. Banco de Dados GO Hub — navegação pública REAL
Entrada: https://db.pokemongohub.net/pt ; confirma links para:
- Tier lists PvP por Grande/Ultra/Mestra e tier lists PvE de atacantes, defensores, por tipo e **Dinamax**;
- Pokédex, stats e tipos, golpes rápidos/carregados de PvP e PvE, ataques de legado/dia comunitário, Pokémon shiny, distance/buddy candy;
- calculadora de CP, chance de Hundo, tabela de efetividade, benefícios Mega/Primal e **comparador de DPS/TDO**.
A lista Grande Liga em https://db.pokemongohub.net/pt/best/great-league exibe colunas ranking, tier e golpes recomendados (por exemplo Cramorant e Altaria nas posições verificadas em 2026-10-09), o que serve para identificar o **rank do meta**, que é **diferente do Rank IV por stat product** já implementado. Dados sujeitos a mudanças/temporadas. Comparador: https://db.pokemongohub.net/pt/tools/comparer ; calculadora PC https://db.pokemongohub.net/pt/tools/cp-calculator ; chances de Hundo https://db.pokemongohub.net/pt/tools/hundo-odds ; bônus Mega https://db.pokemongohub.net/pt/tools/mega-boost.
**IMPORTANTE fonte/licença:** o aviso legal https://db.pokemongohub.net/pt/legal/disclaimer informa © 2017–2026, direitos reservados e créditos a Dialgadex e PvPoke por fórmulas e dados. Isto **não autoriza copiar, republicar, scrapear periodicamente nem integrar toda a base comercialmente**. Priorizar links visíveis com atribuição, dados públicos com licença separada confirmada, fórmulas reimplementadas de forma independente, e solicitar permissão/API oficial se necessário. Separar versão de dados PvP de PvE; nunca introduzir números sem fonte/data.

## 3. Melhorias priorizadas para o PvPPokeGo
| Ordem | Ideia | Ganho | Implementação/limites |
|---|---|---|---|
| P0 | **Avaliar IV calibrado** | Lê barras ATT/DEF/PS quando usuário importa print | Geometria de barras 0–15, escala/orientação, múltiplas capturas reais, confiança + confirmação; nunca extrair IV do apelido |
| P0 | **Ficha avançada e evolução** | PC atual, PC na liga, custos de fortalecimento, evolução e variantes | Completar datasets **com licença e validação**, separar valor certo/estimado e fontes |
| P0 | **Biblioteca unificada de ataques** | energia, DPE, DPS/TDO, efeitos/buffs e tipo | Diferenciar PvP/PvE, precisão de turno/cooldown, valor temporal; testar cálculo em game master |
| P0 | **Ofertas & desejos locais** | Guardar Pokémon desejados/oferecidos, tipo/variante, observações | Sem transações no GO, sem dados precisos por padrão; exportar cartão para compartilhar, sem backend |
| P1 | **Hub de pesquisa / GO Hub** | links úteis de ranking, melhores atacantes/reides, megas e Dinamax | Abrir páginas oficiais com atribuição; nunca copiar banco sem autorização |
| P1 | **Preparador de reides PvE** | Chefe, fraquezas, time próprio, counters, DPS/TDO, horário | Recomendações locais usando datasets verificáveis; não assumir convite automático no jogo |
| P1 | **Duelo/clã/sala offline** | Preparação e compartilhamento da partida, lista de presença, código treinador voluntário | O convite é social e o duelo acontece no GO; sistema de grupo offline/por share sheet |
| P2 | **Comunidade online** | busca, clãs, salas, RSVP, chat, notificações, rank de torneios | Backend + autenticação + privacidade LGPD + bloqueio/denúncia + antispam + suporte a menores |
| P2 | **Mercado de trocas (sem dinheiro)** | Lista filtrável ofertas/desejos e contato voluntário | Sem execução de troca direta, localidade aproximada, respeito às limitações do GO; nunca solicitar credenciais ou oferecer serviços de conta |
| P2 | **Sistema de reputação/medalhas** | Eventos e ranking próprio de usuários | Critérios transparentes, prevenção de fraude, moderação, distinção de Elo oficial do GO |

## 4. UX — lições das imagens
- PokeRaid exibe abas múltiplas em 2 níveis e alguns títulos cortados/quebrados; **não reproduzir** isso no PvPPokeGo. Preferir uma aba Comunidade com 4 chips curtos ("Amigos", "Clãs", "Duelos", "Trocas"), busca e filtros em painel específico, e calendário/reides em seções claras.
- Scanner deve ser contextual e não competir com HUD do overlay durante batalha: trabalhar com fichas quando o Pokémon GO exibe sua página, com opção de cancelar.
- Duas classes de ranking explícitas: Rank IV da instância cadastrada vs tier/rank estratégico meta por liga; não misturar nem usar número não versionado.
- Código de treinador e QR: só armazenar com opt-in, não reutilizar dados pessoais de imagens recebidas nas recomendações de teste.

## 5. QA / Compliance / limites de conclusão
- Não houve benchmark de OCR/calibração, nem ensaio físico, nem inspeção de execução de APK de terceiros. Apenas análise de recursos, nomes de classes/layout e manifest.json externo. Não afirmar segurança ou comportamento completo.
- Nenhum dos XAPK ou seus modelos/assets/dados foi incorporado ao repo privado ou espelho público; nenhum código Kotlin alterado; nenhum APK novo produzido.
- Compartilhamento voluntário, QR treino/amizade/Battle Code diferentes, autorização de localização, exportação/remoção de conta, moderação e antispam devem ser critérios para fases sociais.
- Próxima ação de implementação, **somente quando autorizada/priorizada**: criar branch da 0.5.35 a partir de HEAD validado, atualizar testes/documentação por commit e manter 0.5.34 estável; não construir backend nem gastar com serviços sem decisão explícita do projeto.
