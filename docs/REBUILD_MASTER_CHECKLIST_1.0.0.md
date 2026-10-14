# PvPPokeGo — Checklist Mestre de Reconstrução 1.0.0
**Início:** 13/10/2026  
**Decisão:** reconstruir o aplicativo Android por pequenas etapas, testando cada APK em aparelho antes de avançar. **Este documento é o plano; não significa que a versão reconstruída já exista.**

## 0. Estado real e regras de continuidade
- **Legado preservado:** versão Android 0.5.37 QA/debug, CI SUCCESS em 13/10/2026; **não aprovada ainda em aparelho real**. Versão anterior 0.5.36 igualmente preservada.
- **Código legado:** https://github.com/LucianoToscano/PvPPokeGo/tree/feature/home-mobile-0.5.37-20261013
- **APK legado 0.5.37:** https://drive.google.com/file/d/1Owa3oOXRhUIkrOZj8Q9X5K0Qma1ip8Kk/view?usp=drivesdk
- **Pasta oficial Drive:** https://drive.google.com/drive/folders/1ybogVRRnaOe0KtWjlovxS56bJOi_JewI
- **Fonte histórica de ideias:** [CHECKLIST 1–80 + motor matemático PBE](CHECKLIST-PvPPokeGo-0.5.26.md), [0.5.29](CHECKLIST-PvPPokeGo-0.5.29.md), [0.5.36](CHECKLIST-PvPPokeGo-0.5.36.md), [0.5.37 Home](CHECKLIST-PvPPokeGo-0.5.37-HOME.md).
- **Repositório de build isolado:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build
- **Branch dedicada de planejamento:** rebuild/1.0.0-checklist-20261013.
- [x] Recuperar documentação, lista de funções e referências da última versão estável em CI.
- [x] Criar branch de planejamento independente, sem reverter/remover qualquer código antigo.
- [ ] Preservar, conferir e exportar backups de HUD, equipes, histórico e configurações do dispositivo antes de desinstalar ou trocar de chave de assinatura.
- [ ] Criar projeto Android **limpo e compilável** em módulo/branch de reconstrução, separado dos binários antigos. Não considerar este item pronto por existir a branch de planejamento.
- [ ] Definir tratamento de assinatura e ID de pacote: permitir instalar em paralelo para comparar, ou atualizar in-place somente com chave compatível; não causar perda de dados.
- [ ] Registrar cada release, hash, commit, CI, bugs conhecidos, screenshots e aprovação/recusa do usuário aqui e no Guia Mestre.
- [ ] Nunca marcar função como concluída só porque existia na 0.5.37 ou compilou em CI.

## Legenda de estado para a reconstrução
- [ ] Pendente: ainda não reconstruído/testado nesta nova linha.
- [x] Feito: ação especificamente concluída neste plano (por enquanto apenas documentação/isolamento).
- **Referência existente** = código, ideias ou arte que poderemos estudar/copiar com segurança da 0.5.37, **não** é comprovação de funcionamento na nova linha.
- **Aprovação final de cada função:** código + teste unitário quando aplicável + APK + teste manual em aparelho + evidência visual + regressão das funções anteriores.

## Organização de imagens: concluída como preparação, não implementação

- [x] Extrair atlas com 1.167 sprites individuais do APK 0.5.37 e preservar o launcher PNG original.
- [x] Inventariar 194 raster originais embutidos no APK e vetores Android compilados.
- [x] Identificar e separar os dois backgrounds **originais do usuário** e as capturas de referência.
- [x] Criar a [biblioteca de imagens e índice](IMAGE_ASSET_LIBRARY_0.5.37.md), pasta do Drive e ZIP de recursos do APK verificado.
- [ ] Incorporar os dois fundos e ícones aprovados à NOVA base 1.0.0 e conferir em aparelho (ainda não feito).

## Etapa 01 — Android mínimo e identidade (primeiro APK)
- [ ] Aplicativo Android Kotlin + Jetpack Compose inicia e reabre sem crash.
- [ ] Ícone oficial do PvPPokeGo fiel à referência e ícones originais restaurados (não inventar emoji substituto).
- [ ] Logo «PvPPokeGo» com PvP vermelho, Poke branco, Go azul/ciano; qualidade nítida.
- [ ] Uma tela inicial nativa vertical, não layout de site; funcionamento em largura pequena.
- [ ] **Background 1:** estádio verde amplo fornecido pelo usuário para a Home, com crop proporcional e legibilidade.
- [ ] **Background 2:** estádio com círculo azul/vermelho central para Equipe/Arena.
- [ ] Cards azul-escuros translúcidos com bordas leves/ciano; preservar estilo clean e contraste.
- [ ] Aplicar camada escura/blur moderado sem perder o fundo; desempenho estável.
- [ ] Respeitar barras do sistema Android, cutout/notch, teclado, gesto voltar e rolagem.
- [ ] Menu inferior real: Início, Equipe, Histórico, Ajustes, Sobre (Arena pode vir depois).
- [ ] Botões abrem destinos reais; nada de superfícies decorativas fingindo funcionar.
- [ ] Testar: instalar, tocar em todas as abas, suspender/reabrir, navegar 10x, screenshot comparativa.
**Critério de saída:** APK 1.0.0-minimo aprovado em aparelho. Nada de OCR/overlay nesta etapa.

## Etapa 02 — Configurações locais / perfil
- [ ] Nome do treinador editável, salvo entre sessões.
- [ ] Avatar/foto configurável, com permissão segura; placeholder neutro quando ausente.
- [ ] Nível do treinador (validado) e equipe/facção, se útil.
- [ ] Configurações locais persistidas; estados e opções não voltam ao padrão após fechar.
- [ ] Estatísticas reais: partidas, vitórias, derrotas, empates, maior sequência, sem números inventados.
- [ ] Poeira estelar e outras informações **somente se informadas pelo jogador** ou de origem autorizada.
- [ ] Sobre: versão/build/desenvolvedor, explicações, privacidade e diagnóstico.
- [ ] Exportar/importar configurações com versão de schema e backup antes de sobrescrever.
- [ ] Testar retorno da tela, edição, reinício, entradas inválidas e restauração.

## Etapa 03 — Equipe manual, três Pokémon
- [ ] Home mostra **exatamente 3 vagas** clicáveis, sem criar quarto card ou abas desnecessárias.
- [ ] Vaga vazia exibe «+ Adicionar Pokémon» e não dados fictícios.
- [ ] Seleção por busca de espécie + forma existente no banco offline.
- [ ] Imagem/sprite oficial do atlas local ou placeholder claro, sem desaparecer se OCR falhar.
- [ ] Nome, PC, PS/HP e IVs A/D/S (0–15) com campos opcionais e valores validados.
- [ ] Sexo: símbolos ♂ azul/♀ rosa sem fundo; alternância conforme suporte da espécie.
- [ ] Opção **Sombroso**: desligada cinza / ligada colorida; ícone discreto sem fundo.
- [ ] Opção **Mega**: desligada cinza / ligada colorida; apenas formas legalmente disponíveis.
- [ ] Nome central e alinhamentos simétricos; Rank em cima, PC abaixo; PS/HP/IV bem distribuídos.
- [ ] Ataque rápido, carregado 1 e carregado 2 com nomes, tipos, custo/energia.
- [ ] Exibir rank PvP **apenas calculado por fonte e IV válidos**, nunca como texto inventado.
- [ ] Liga Grande, Ultra, Mestra e Automático; limite de PC, confirmação de incompatibilidade.
- [ ] Cálculo de média de PC real, equipe completa/incompleta.
- [ ] Trocar a ordem 1/2/3, remover e salvar equipe sem sobrescrever slots por engano.
- [ ] Modo manual NÃO se apresenta como reconhecimento visual confirmado.
- [ ] Testar equipe vazia, parcial, 3 preenchidos, CP igual, formas, sexo, Mega e Sombroso.
**Critério:** adicionar, editar, salvar, remover e reabrir os 3 Pokémon no próprio celular.

## Etapa 04 — Fontes de dados e sprites
- [ ] GameMaster offline atualizado, versionado, com espécies, CP, tipos, formas e golpes.
- [ ] Dados PvPoke/estatísticas/CPMs ancorados por versão e licença.
- [ ] Atlas de ícones de Pokémon com índice válido, incluindo variantes suportadas.
- [ ] Ícones de tipos, ataques, escudo, liga, olho, cadeado, sombroso e Mega fiéis às referências.
- [ ] Pesquisa PT-BR/EN, correspondência de nomes e formas.
- [ ] Tipos simples/duplos, fraquezas, resistências, STAB e tabelas matemáticas testadas.
- [ ] Cache offline: nenhuma busca de internet crítica durante uma batalha.
- [ ] Atualização de banco remota somente posteriormente, com integridade/verificação.
- [ ] Tests: espécie inexistente, forma regional, ícone ausente, moveset impossível e PC máximo.

## Etapa 05 — Overlay Android: autorização e início/parada
- [ ] Solicitar permissão sobre outros aplicativos com fluxo claro e retorno seguro.
- [ ] Fluxo MediaProjection/screen capture separado do overlay.
- [ ] Serviço foreground/notification coerentes com Android 13–16 e ciclo de vida.
- [ ] Botão Iniciar Overlay / Parar Overlay mostra estado real, não demonstração.
- [ ] Exibir HUD sobre Pokémon GO sem interceptar toques destinados ao jogo.
- [ ] Retorno ao app, tela bloqueada, minimizar, pausa, retomar e encerramento tratados.
- [ ] Rotação/resolução: recriar VirtualDisplay/áreas de captura se necessário.
- [ ] Modo compatibilidade com gravação externa sem bloquear uso padrão.
- [ ] Teste no aparelho: iniciar, parar, voltar, reabrir, girar e continuar sem UI travar.

## Etapa 06 — HUD, controles e editor visual
- [ ] HUD configurável com blocos de aliado, adversário, reservas e ataques.
- [ ] Olho original: exibir/ocultar sem emoji do sistema sobreposto.
- [ ] Cadeado original: bloquear/desbloquear, com região de toque exatamente no desenho.
- [ ] Arrastar livremente os blocos somente em modo edição/desbloqueado.
- [ ] Botões pequenos com hit-target funcional sem bloquear ações do Pokémon GO.
- [ ] Menu edição com confirmar/cancelar/reiniciar posição, sem esconder controles.
- [ ] Botão de configurações e liga acessível; sem zonas mortas.
- [ ] Escala, opacidade, posição e layout persistidos.
- [ ] Backup/exportação/importação das posições do HUD.
- [ ] Ícones liga, tipo, HP, energia, ataques, reservas, escudo, carregado e aviso alinhados.
- [ ] Textos de golpes menores, simétricos e legíveis; interface compacta/clean.
- [ ] Modos Minimal, Normal e Avançado (posterior) sem poluição visual.
- [ ] Testes: toque sobre cada ícone, olho/cadeado, 3 resoluções, escalas, modo edição e rollback.

## Etapa 07 — Captura e reconhecimento visual
- [ ] Capturar frames confiáveis com timestamps e política de pausa/retorno.
- [ ] OCR de nomes e CP de aliado e inimigo, sem alterar batalha a partir de OCR isolado.
- [ ] Reconhecimento visual por sprites/fingerprints com pontuação/confiança.
- [ ] Distinguir espécie vs forma vs regional vs sombra vs mega sem falsas certezas.
- [ ] Recusar associações espécie+PC matematicamente impossíveis.
- [ ] Reconhecer Pokémon ativo, três membros do time e troca de posição.
- [ ] Reconhecimento das duas reservas aliadas com cards/imagens estáveis.
- [ ] Reconhecer inimigo e acompanhar o oponente após troca.
- [ ] Proteção contra leituras parciais/desatualizadas e candidatos ambíguos («Analisando»).
- [ ] Modo automático/manual selecionável e com estados explicitamente separados.
- [ ] Testar screenshots/vídeos de aparelho e registrar taxas de acerto e falhas.

## Etapa 08 — Scanner de ficha/Avaliar
- [ ] Importar screenshot da ficha Pokémon GO com seletor Android.
- [ ] Reconhecer nome, CP, HP, tipo, forma e golpes, exigindo revisão.
- [ ] Scanner da tela «Avaliar» para inferir IV 0–15 com calibração de barras real.
- [ ] Selecionar explicitamente vaga 1, 2 ou 3 antes de aplicar.
- [ ] Não substituir dados salvos sem confirmação do jogador.
- [ ] Ranking de IV por liga usando dados locais, identificando aproximações.
- [ ] Histórico de scans local, opção limpar/exportar, sem guardar imagens sem necessidade.
- [ ] Testar screenshots com escalas, tema, idioma, enquadramento e IV extremos.

## Etapa 09 — Matcher de reservas e continuidade
- [ ] Vincular observação à identidade do Pokémon correto: speciesId, forma e CP.
- [ ] Não confundir dois Pokémon com CP igual, nem atribuir reserva por eliminação.
- [ ] Diferenciar «sinal candidato» de «identidade confirmada» com limiar e evidências.
- [ ] Separar Pokémon aliado ativo, reserva 2/3, inimigo ativo e reservas inimigas.
- [ ] Troca do aliado, troca do adversário e trocas simultâneas.
- [ ] Retorno ao mesmo Pokémon restaura energia, HP e movimento conhecidos.
- [ ] Desmaio, início no meio da batalha, fim, re-match e novo combate.
- [ ] Falha de um frame ou OCR espúrio não inicia partida nova.
- [ ] Mesmo oponente em duas partidas não deve fundir histórico automaticamente.
- [ ] Testes de regressão das políticas BattleCorrespondenceTracker e ReserveCardMatcher.

## Etapa 10 — Golpes e contagem de energia
- [ ] Mostruário do golpe rápido e dois carregados com ícone de tipo e nomes PT-BR.
- [ ] ▼ seletor de golpes inimigos; melhores golpes padrão com opção de corrigir manualmente.
- [ ] Contar Fast Moves: turns de 0,5s, duração, ganho de energia.
- [ ] Deduplicar eventos e não somar energia por animação/frames repetidos.
- [ ] Exibir quantos Fast faltam para cada carregado (+N).
- [ ] Energia min/provável/max para inimigo com Fast não identificado.
- [ ] Residual, overfarm, limite de 100 e double-ready/back-to-back.
- [ ] Reiniciar contagem na nova batalha sem apagar registro anterior.
- [ ] Relacionar gasto de energia ao carregado observado, não ao presumido.
- [ ] Verificar switch e retorno do inimigo com energia preservada.
- [ ] Testes unitários determinísticos para todos os golpes e casos-limite.

## Etapa 11 — Escudos, HP e dano
- [ ] Escudos aliados e inimigos 0/1/2; observação real sem descer/voltar sozinho.
- [ ] Detecção Charged usado, escudado ou não escudado.
- [ ] HP atual/HP máximo, redução estimada e «barra fantasma» de dano sobre HP.
- [ ] Diferenciar dano observado, calculado, estimado e desconhecido.
- [ ] Modelar STAB, tipos, clima se aplicável, shadow, estágios de ataque/defesa.
- [ ] Debuffs/buffs por golpe e probabilidade real de ocorrência.
- [ ] Estimativa de KO, HP que sobraria e faixa de incerteza.
- [ ] Escudo: SHIELD / HOLD / OPTIONAL com justificativa curta.
- [ ] Verificar OCR de escudos/HP em gravações; não tratar sempre escudos como 2.
- [ ] Barras de energia e HP legíveis, sem cobrir controles nativos do jogo.

## Etapa 12 — Motor matemático/preditivo (reintroduzir em módulos)
- [ ] Snapshot único por turno: tempo, Pokémon, energia, HP, escudos, golpes, formas.
- [ ] Tracker fase do Fast + correção de latência e frames perdidos.
- [ ] Hipóteses de moveset e Fast com evidência/cadência/dano.
- [ ] Forecast Charged: READY, +N, janela de ameaça e intervalo de confiança.
- [ ] Threat score combina prontidão, dano, janela de reação e incerteza.
- [ ] ShieldDecisionEngine integrado às observações de batalha.
- [ ] Farm Down, Overfarm, optimal throw e timing de carregados.
- [ ] CMP por estimativas de Attack/IV/CP (sem certeza falsa sobre IV oculto).
- [ ] Sac Swap por janela de turnos; não sugerir após início do Charged.
- [ ] Bait como hipótese, não como intenção confirmada.
- [ ] Copiloto visual: continuar Fast, trocar, escudar, não escudar e alertas úteis.
- [ ] Dicas de estratégia contextualizadas, sem controlar o Pokémon GO.
- [ ] Métricas de acerto previsão/resultado, incluindo falsos positivos.
- [ ] Evolução futura: Bayesian predictor, padrões locais suavizados e simulação curta.
- [ ] Reaproveitar como referência os testes PBE do checklist original; portá-los modularmente.

## Etapa 13 — Histórico, treinamento e backups
- [ ] Sessões com data, duração, liga, nomes, PC, formas, turnos e eventos cronológicos.
- [ ] Salvar golpes usados, quantidade e momento estimado/observado.
- [ ] Registrar trocas, escudos, HP, energia e avisos do copiloto com origem/confiança.
- [ ] Resultado vitória/derrota/empate/desconhecido apenas quando confirmado.
- [ ] Histórico local navegável e filtrável por oponente/espécie/liga/data.
- [ ] Resumo pós-batalha: acertos, erros, oportunidades e recomendações.
- [ ] Comparar previsões com realidade, sem apresentar palpites como fatos.
- [ ] Exportar histórico em JSON/CSV/TXT/ZIP e importar quando seguro.
- [ ] Exportar diagnóstico + eventos + Logcat do app, com dados pessoais protegidos.
- [ ] Nunca sobrescrever ou mesclar duas batalhas sem evidência.
- [ ] Replay offline de imagens/eventos (futuro), dataset real com consentimento e métricas de precisão.
- [ ] Testar limpar, salvar, reinstalar/restaurar e histórico com várias batalhas.

## Etapa 14 — Arena local e recursos opcionais
- [ ] Arena local: atividade, progresso, medalhas, sequências e recordes.
- [ ] Ranking competitivo online real: **planejado, depende de backend/contas/moderação**.
- [ ] Recompensas reais, PokéCoins, passes: **não implementadas e nunca prometer distribuição sem parceria/infraestrutura**.
- [ ] Comunidade: perfis, amigos, chat, clãs e campeonatos (escopo futuro).
- [ ] Recursos PvE/Rocket/reides/Dinamax e preparador de times (futuro).
- [ ] Calendário/eventos com fuso opt-in e dados fontes confiáveis (futuro).
- [ ] Perfis compartilháveis, exportação de equipes e ranking offline, se útil.
- [ ] Estatísticas pessoais ou comunidade somente opt-in e com política de privacidade.

## Etapa 15 — Qualidade, desempenho, segurança, entrega
- [ ] Testes Kotlin unitários, integração de dados e smoke tests Compose.
- [ ] APK Standard com SHA-256, assinatura verificada e relatório de assets.
- [ ] Teste físico com Android real: mínimo Android 10; alvo Samsung/Android 16.
- [ ] Provar consumo de bateria/memória/CPU tolerável com captura ativa.
- [ ] Checar permissões negadas, notificações bloqueadas e erros recuperáveis.
- [ ] Captura segura; sem automação de toques no jogo, sem hooks indevidos.
- [ ] Não vazar dados privados para mirror público ou logs compartilhados.
- [ ] Atualizar documentação, changelog, versionCode, checklist e Guia Mestre.
- [ ] Enviar APK à pasta correta no novo Drive e verificar upload/sha.
- [ ] Teste usuário aprovado: NÃO regredir função que já recebeu aceite.
- [ ] Fazer rollback simples para a última versão aprovada.

## Ordem de versões (proposta — não são releases prontas)
| Versão | Uma entrega central | Estado |
|---|---|---|
| 1.0.0 | App limpo + logo + duas artes de background + Home e navegação | PENDENTE |
| 1.0.1 | Perfil + preferências locais | PENDENTE |
| 1.0.2 | Três vagas da equipe com cadastro e salvamento | PENDENTE |
| 1.0.3 | Banco offline, espécies, sprites, tipos e ligas | PENDENTE |
| 1.0.4 | Overlay: permissão, serviço, iniciar/parar | PENDENTE |
| 1.0.5 | HUD editável, olho, cadeado, toque e backup | PENDENTE |
| 1.0.6 | Captura/OCR base sem alterar estado sem confirmação | PENDENTE |
| 1.0.7 | Scanner de ficha e Avaliar | PENDENTE |
| 1.0.8 | Reconhecimento aliado/inimigo e reservas | PENDENTE |
| 1.0.9 | Matcher, continuidade e troca de Pokémon | PENDENTE |
| 1.0.10 | Golpes, contadores e energia | PENDENTE |
| 1.0.11 | Escudos, HP, dano e previsão | PENDENTE |
| 1.0.12 | Copiloto e motor preditivo modular | PENDENTE |
| 1.0.13 | Histórico e aprendizagem pós-batalha | PENDENTE |
| 1.0.14+ | Arena e adicionais selecionados após aprovação | PENDENTE |

**Regra:** a numeração pode mudar após avaliar dependências; uma versão pode conter correção de bug, mas **não misturar múltiplas funções grandes**. Primeiro ter um APK mínino que abre e navega; só depois mexer em reconhecimento e previsão.

## Mini-checklist obrigatório para CADA APK
- [ ] Fonte correta e commit fixado, sem descartes.
- [ ] Apenas a função da etapa + correções necessárias.
- [ ] Código compila e testes unitários passam.
- [ ] APK assinado e hash conferido; link real validado.
- [ ] App abre, fecha, volta e navega sem travar.
- [ ] Função nova testada no celular em caso normal + caso erro.
- [ ] Funções das versões já aprovadas continuam funcionando.
- [ ] Screenshot do resultado + log/bug, se falhar.
- [ ] Feedback: APROVADO / AJUSTAR / REPROVADO; somente então próxima etapa.
- [ ] Checklist, Guia Mestre e Drive atualizados com a evidência.

## Assets e referências de visual aprovados para consultar
- Foto 1 da conversa de 13/10/2026: estádio verde panorâmico em orientação retrato — fundo **Home**.
- Foto 2 da conversa de 13/10/2026: estádio/arena com círculo azul-vermelho central — fundo **Equipe/Arena**.
- Mockups de telas Início, Configurações e «Sua Equipe» enviados pelo usuário nas conversas anteriores (referências de layout, não capturas funcionais).
- ZIP local de ícones originais fornecido no projeto: PvPPokeGo_0.5.10_ORIGINAL_ICONS.zip.
- Ícone launcher original restaurado e atlas de Pokémon do repositório legado; nenhuma alteração de ícone sem aprovação.

## Primeira tarefa após a aprovação do checklist
**Construir 1.0.0:** estrutura Android limpa, launcher oficial, fundo 1 Home, fundo 2 Equipe, navegação simples, cards vazios do time com +. *Não* adicionar Overlay, OCR, ranking, previsão, contador ou recompensa ainda.

## Protocolo de acompanhamento
A cada etapa, registrar nesta tabela:

| Etapa / APK | Commit | Build + hash | QA aparelho | Decisão do usuário | Bugs/pendências |
|---|---|---|---|---|---|
| 1.0.0 | — | não gerado | não testado | aguardando | — |

**Última atualização deste plano:** 13/10/2026. Checklist inventaria recursos do legado; nenhum APK 1.0.x foi compilado ou instalado nesta etapa documental.


## Complemento da biblioteca visual — ícones de HUD, tipos e rank (13/10/2026)

Ícones dos **18 tipos** foram exportados dos contornos originais `TypeGlyphPaths.kt`; cadeado, olho, matchup (forte/neutro/fraco) foram convertidos dos métodos Canvas de `BattleOverlayView.kt` para PNG/SVG, com arquivo fonte preservado. Ranking no APK 0.5.37 é texto/emoji, portanto os badges #5/#18/#12 presentes nas imagens de referência foram incluídos SOMENTE como recortes de mockup, claramente rotulados. A extração anterior da biblioteca era incompleta por considerar só PNG embutido.

**Pasta Drive:** https://drive.google.com/drive/folders/10svDEu4W1iCMkiO5attXCHduNEmW2NnP

**Pacote só de ícones:** https://drive.google.com/file/d/1pzibhWTMq6Rknt23D5W2vbwncVD5ciyZ/view?usp=drivesdk

**Biblioteca completa V2:** https://drive.google.com/file/d/1xSfd7xzGQspC4etEJruMgF5ZXcs0VCKM/view?usp=drivesdk

**Mostruário:** https://drive.google.com/file/d/1RbGH90QkKQ8rSLGN5-CZuTzKZurbKBes/view?usp=drivesdk

**Inventário/documentação:** [ICON_ASSET_HUD_TYPES_RANKS_0.5.37.md](ICON_ASSET_HUD_TYPES_RANKS_0.5.37.md)

Status: pacote PNG/SVG criado, ZIP testado íntegro, Drive upload confirmado; não significa que novos sprites estão integrados em APK 1.0.0. Antes de utilizar esses ícones na reconstrução, conferir visual e toque em hardware.

---

