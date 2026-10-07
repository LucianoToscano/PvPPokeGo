# Changelog

## 0.5.25

- Corrigida a exibição dos ícones/retratos dos Pokémon de reserva: cache estável por slot da equipe, fallback pelos cards nativos e ícones mantidos também na tela de troca; o recorte duplicado que podia deixar miniaturas quase vazias foi removido.

- Nesta etapa, a validação/build foi reduzida ao APK `standard`; gravação interna e Recorder Compat permanecem no código, sem remoção, mas ficam para uma etapa posterior conforme decisão do projeto.
- Adicionada configuração de assinatura permanente também no Gradle/Android Studio via `keystore.properties` local ou variáveis `PVPPG_*`; o repositório continua sem armazenar a chave privada.
- Adicionados scripts reproduzíveis para `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug` e verificação da integridade/assets do APK.

- Consolidação criada a partir de `release/0.5.24` sem alterar a baseline congelada `release/0.5.20`.
- Corrigida a divergência de versionamento: a linha mais nova estava na branch 0.5.24, mas ainda declarava `versionName 0.5.23`; a entrega consolidada passa a `versionName 0.5.25` e `versionCode 30`.
- Restaurado, de forma seletiva, o thumbnail ao vivo dos dois cards de reserva que existia na linha 0.5.23 e havia sido perdido na 0.5.24. O atlas offline continua sendo a fonte visual principal quando espécie/forma está confirmada; o crop ao vivo é apenas fallback de apresentação.
- Os dois blocos de reserva permanecem visíveis mesmo antes da identidade ser confirmada. Sem confiança suficiente mostram `ANALISANDO` e não exibem `MELHOR`/`EVITAR`.
- Preview de dano passou a explicitar simultaneamente o cenário `SEM ESC`, HP restante/faixa, risco de KO e o cenário `COM ESC` quando um escudo estiver disponível.
- Launcher passa a usar os recursos adaptive/round oficiais, cujo foreground referencia a mesma arte oficial `pvppokego_launcher`, melhorando consistência em launchers Samsung/Android.
- Mantida a separação de manifests: APK padrão sem AccessibilityService/specialUse; Recorder Compat mantém somente o fallback de screenshots por Acessibilidade.
- Novo pipeline 0.5.25 audita assets/atlas/PvPoke, manifests, TODO/FIXME, executa testes das duas variantes, compila, valida APKs com `unzip -t`, calcula SHA-256 e empacota build/projeto Android Studio.
- Estratégia de assinatura permanente preparada no CI: releases podem ser assinados por um keystore injetado exclusivamente por GitHub Secrets; nenhuma chave privada ou senha é armazenada no repositório. Na ausência desses secrets, o pipeline gera apenas APKs QA/debug claramente identificados.

## 0.5.23

- Pipeline de validação restaurado para o runner macOS já comprovado nas 0.5.21/0.5.22, mantendo hashes portáveis com `shasum -a 256`.

- Novo motor preditivo de energia inimiga: `EnemyEnergyTracker` mantém hipóteses discretas por Fast Move legal, energia residual, turns, contagem e confiança; não converte spam de toques em energia.
- `ChargedMovePredictor` calcula antecipadamente, para todos os Charged Moves legais da espécie/forma, FAST restantes, turns, janela mais cedo/provável, READY possível/certo, dois Chargeds armazenados, EPT/DPT/DPE, bait, pressão de escudo e risco de KO/farm-down.
- Ataque carregado revelado corrige o estado futuro: hipóteses impossíveis são eliminadas, custo é descontado só das compatíveis e Charged não identificado ramifica pelos custos legalmente possíveis.
- Lacunas de captura e Fast Moves possivelmente perdidos não são convertidos em certeza: o estado é alargado com hipóteses conservadoras, inclusive durante a sequência do Charged do próprio jogador.
- Energia por Pokémon continua preservada através de trocas; uma nova batalha limpa o estado e stages de Attack/Defense zeram ao sair de campo.
- `ReactionWindowEstimator` mede latência captura→análise, usa p95 quando há amostras e soma orçamento explícito de render, reação humana e margem de segurança para avisar PREPARE-SE antes do Charged.
- Overlay do inimigo mostra energia preditiva compacta e até duas ameaças (READY / +N FAST / tempo aproximado), sem transformar o HUD em planilha.
- Preview fantasma de HP pode aparecer antes da animação do Charged, mostrando a faixa estimada de dano sem escudo e HP restante.
- `DamageForecastEngine` integrado ao predictor com STAB, efetividade, Shadow, stages -4..+4 e o método PvPoke `percentMaxHP`; dano observado continua tendo prioridade.
- Game Master passa a ler buffs/debuffs, chance, alvo, archetype e movepools legais completos; proc probabilístico vira faixa de stages em vez de falsa certeza.
- CMP permanece separado dos buffs, usando Attack de batalha para desempate como na implementação atual do PvPoke.
- Histórico periódico guarda energia preditiva, hipóteses, lead time, ameaça, READY, bait, shield pressure e risco de KO para análise pós-batalha.
- Padrões de batalhas anteriores permanecem locais; nenhum histórico do usuário é enviado automaticamente a servidor/IA.
- Snapshot PvPoke fixado no commit `f627e89e53c0c7b903fff097df7a0ad0ac95decc`; metadados do build registram data, turn=500 ms, settings de buffs e SHA-256 dos datasets.
- Performance reforçada com cache de movepools e stats e atualização incremental por evento, com limite de hipóteses.
- Gravador interno mantém confirmação por quadro realmente codificado e fallback AVC conservador; captura stale pausa recomendações em vez de continuar sobre frames antigos.
- Histórico tem checkpoint de sessão ativa e recuperação curta após interrupção, além de resumo de time/momentos-chave.
- Testes determinísticos adicionados/expandidos para Fast 1/2/3/4 turns, energia residual, overfarm, Chargeds consecutivos, hipóteses, deduplicação, lag/evento perdido, switch/restore, nova batalha, buffs/debuffs, CMP, Shadow, shields e dano.


## 0.5.22

- Corrigido o início do overlay no APK padrão após a separação do modo Recorder Compat.
- O APK padrão volta ao caminho de foreground service comprovado na 0.5.19: somente `mediaProjection`, sem `specialUse`.
- `FOREGROUND_SERVICE_SPECIAL_USE` e `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` ficam exclusivos da variante Recorder Compat.
- Se o Android revogar a MediaProjection no APK padrão, o serviço encerra de forma limpa em vez de permanecer ativo sem uma fonte de captura.
- A variante Recorder Compat preserva o fallback por screenshots de Acessibilidade para gravadores externos.
- Nenhuma lógica de OCR, reconhecimento visual, correspondência/continuidade, reservas, energia, Charged, FAST, histórico ou HUD foi removida.


## 0.5.21

- Corrigido o bloqueio de instalação mostrado pelo Google Play Protect: o APK principal não declara nem empacota mais o serviço de Acessibilidade usado para captura de compatibilidade.
- A compatibilidade com Samsung Screen Recorder/gravadores externos foi isolada em uma variante separada `Recorder Compat`, preservando a implementação sem obrigar o APK principal a solicitar acesso sensível.
- O APK padrão mantém overlay, OCR, reconhecimento visual, correspondência/continuidade de batalha, histórico, exportações, PvPoke offline e gravação interna.
- `Icon PVPPokeGo.png` passa a ser referenciado diretamente como `android:icon` e `android:roundIcon`, sem depender do adaptive icon para o instalador/launcher.
- Gravador interno reforçado para aparelhos Samsung: saída AVC limitada a dimensões alinhadas em macroblocos e no máximo 720x1280, removidas restrições opcionais de profile/bitrate mode que quebram alguns codecs OEM.
- MP4 interno agora é validado por duração e dimensões antes de ser publicado em Movies/PvPPokeGo; arquivos inválidos não são apresentados como gravação concluída.
- A versão e os nomes dos arquivos exportados passam a usar `BuildConfig.VERSION_NAME`, evitando divergências entre APK e README/ZIP.
- Preservadas integralmente as correções da 0.5.20: reconhecimento visual por espécie/forma, matcher de reservas, continuidade de batalha, deduplicação de carregados, energia por Pokémon, histórico detalhado e banco visual offline.


## 0.5.20

- Adicionado terceiro sinal de identidade por imagem do próprio Pokémon, além de OCR e PC.
- Novo banco visual offline de fingerprints por National Dex, gerado a partir de renders HOME do PokeAPI; o APK armazena apenas fingerprints compactos, não o catálogo de imagens.
- Os três cards da equipe usam fusão independente de OCR + PC + imagem; uma leitura visual conflitante impede que um frame redefina silenciosamente o time.
- A identidade interna agora preserva o speciesId/form exato do PvPoke durante time, ativo, reservas, trocas e restauração de energia.
- Rankings e movesets procuram primeiro o speciesId exato e só depois o nome exibido, evitando misturar formas que compartilham nome/Dex.
- Reservas registram a origem da confirmação (PC, imagem da forma, imagem/Dex ou eliminação) e não exibem matchup confiável antes da confirmação.
- Adicionados testes de continuidade entre formas regionais e de preservação independente da energia/estado por forma.
- Reconhecimento visual só vira identidade direta quando o número da Pokédex é inequívoco no banco; formas regionais/alternativas continuam exigindo OCR/PC para não errar tipo.
- Tipos e matchup continuam vindo do banco PvPoke depois que a identidade é confirmada, então funciona com times diferentes e contas diferentes.
- Adicionado modo de compatibilidade com gravadores Samsung/externos via Accessibility screenshot API.
- O serviço de acessibilidade do PvPPokeGo não lê nós da interface e não executa toques; é usado apenas como fonte alternativa de screenshots.
- Quando outro gravador revoga a MediaProjection, o PvPPokeGo não encerra mais o serviço: mantém o HUD e migra para capturas de compatibilidade se o serviço estiver habilitado.
- Novo botão "Iniciar overlay para gravador Samsung / externo" inicia o PvPPokeGo sem abrir MediaProjection própria, evitando disputa direta com o gravador.
- No modo externo, OCR, identidade, tipos e matchup continuam; a contagem FAST de alta frequência fica reduzida pela taxa de screenshots do Android.
- Mantido o ícone oficial Icon PVPPokeGo.png como adaptive launcher icon.
- Mantidas as correções 0.5.19 para cards por PC, tipos sanitizados, package real e todas as melhorias anteriores.


## 0.5.19

- Corrigida a causa raiz dos tipos errados nas reservas: os 3 cards da tela "Escolha a sua equipe" passam a ser lidos separadamente por coluna, preservando o par Pokémon + PC.
- O caso Houndoom PC1499 / Cramorant PC1485 / Rillaboom PC1500 deixa de deslocar os PCs entre os slots.
- OCR principal de jogador, adversário e texto de batalha passa a rodar em paralelo para reduzir latência.
- OCR dos dois cards de reserva usa duas regiões pequenas e independentes, evitando troca de ordem entre os PCs.
- Reserva com PC único conhecido é confirmada imediatamente; OCR contínuo do card para assim que a identidade estiver confirmada.
- Recomendações e indicadores de troca só usam reservas com identidade confirmada.
- Indicadores da tela de troca permanecem presos ao card original, sem deslocar esquerda/direita quando um slot ainda não foi confirmado.
- Tipos do PvPoke são sanitizados: o pseudo-tipo "none" é removido. Rillaboom passa a exibir somente Planta.
- Testes adicionados para Rillaboom=Planta, Cramorant=Voador/Água e Houndoom=Sombrio/Fogo.
- Ícone do aplicativo migrado para adaptive icon/mipmap, usando a arte oficial já enviada pelo usuário.
- O APK de teste deixa de usar applicationId ".debug" e volta a instalar no pacote real com.lucianotoscano.pvppokego, evitando dois PvPPokeGo diferentes no aparelho.
- Mantidas as melhorias do histórico/exportação da 0.5.18 e todos os recursos anteriores.


## 0.5.18

- Histórico de batalhas redesenhado em cartões mais legíveis.
- Resumo por batalha separa ataques rápidos, carregados, decisões do assistente e diagnósticos.
- Linha do tempo usa blocos visuais distintos para VOCÊ, INIMIGO e ASSISTENTE.
- Fontes técnicas são traduzidas para descrições legíveis, como "leitura da tela" e "queda de HP do inimigo".
- Diagnóstico técnico fica oculto por padrão e pode ser expandido separadamente.
- Nova exportação de TODO o histórico em ZIP diretamente pela tela do histórico.
- Cada batalha também pode ser exportada isoladamente.
- O ZIP inclui historico-legivel.txt, historico.json, historico.csv e README.txt.
- O formato foi pensado para ser enviado diretamente ao ChatGPT ou para análise externa.
- Mantidas as correções da 0.5.17 para ícone oficial, notificação rápida e identidade/tipos das reservas por CP.


## 0.5.17

- Ícone oficial enviado pelo usuário aplicado como ícone do APK/launcher.
- Notificação persistente ganhou ícone grande do PvPPokeGo, estado ON/PAUSADO/OCULTO e ações rápidas no estilo Poke Genie/IVision.
- As três ações prioritárias da notificação são Pausar/Retomar visão, Remover/Mostrar sobreposição e Parar serviço.
- OCR passa a ler o CP dos dois cards nativos de reserva durante a batalha.
- Tipos/matchup das reservas são associados ao Pokémon pelo CP real do card, em vez de depender de rotação presumida dos slots.
- Após troca do Pokémon ativo, a associação dos cards é invalidada e confirmada novamente por OCR.
- Dois resultados iguais de CP são exigidos antes de a associação virar autoritativa.
- Quando a identidade de um card ainda não está confirmada, o HUD mostra estado desconhecido/IDENTIFICANDO em vez de exibir um tipo errado.
- Detecção do time foi reforçada para preservar pares nome+CP quando aparecem na mesma linha ou em linhas adjacentes.
- Testes adicionados para o caso Houndoom / Cramorant / Rillaboom e trocas de Pokémon.
- Mantidas gravação interna, PvPoke offline, detector híbrido e diagnóstico da 0.5.16.


## 0.5.16

- Novo detector híbrido de Fast Move: combina queda de HP, microqueda, cadência esperada e pico de movimento com confiança.
- Testes unitários adicionados para HP forte, microqueda, fallback por movimento/cadência e proteção contra dupla contagem.
- PvPoke offline no APK: gamemaster + rankings 1500/2500/10000 são empacotados no build, com fallback de rede/cache.
- Exportação de diagnóstico em ZIP com configurações, posições, histórico, confiança/origem dos eventos e dados do aparelho.
- Gravação de tela interna do PvPPokeGo: reutiliza os mesmos frames da captura, evitando abrir uma segunda MediaProjection.
- Vídeos internos são salvos em Movies/PvPPokeGo, sem áudio.
- A gravação pode ser iniciada/parada pelo menu rápido, tela principal ou notificação.
- Durante gravação, pausar a análise não interrompe o vídeo.
- Mantidas todas as correções da 0.5.15: posições, ícone de Configurações, golpes mais próximos e carregamento inimigo corrigido.


## 0.5.15

- As posições do HUD passam a usar como preset oficial as coordenadas exportadas no arquivo PvPPokeGo-0.5.14-config.json enviado pelo usuário.
- Ataque ágil do inimigo aproximado levemente dos dois ataques carregados: distância vertical reduzida de 123 para 112 unidades de referência.
- Áreas de toque dos ataques carregados reposicionadas junto com o visual para manter clique e desenho alinhados.
- Engrenagem customizada removida; launcher passa a usar o ícone padrão de Configurações/Preferências do Android.
- Ícone de configurações mantém uma hitbox confortável, mas com desenho visível pequeno, próximo ao porte do olho/cadeado.
- Mantidos menu reorganizado e controles rápidos de Pausar visão, Remover sobreposição e Parar serviço da 0.5.14.
- Backup exportado passa a usar PvPPokeGo-0.5.15-config.json.
- Corrigida contagem de energia dos carregados inimigos para ataques rápidos de baixo dano: microquedas de 1 px da barra de HP agora são acumuladas em vez de descartadas.
- Rastreamento de energia agora rejeita Fast Move com ganho de energia zero e usa um Fast Move válido como fallback.


## 0.5.14

- Engrenagem redesenhada novamente: menor, sem o círculo grande e com proporção visual próxima ao olho/cadeado.
- Área de toque da engrenagem continua confortável, mas o desenho visível fica discreto.
- Menu da engrenagem reorganizado em seções: Batalha, HUD/Visual, Análise e Sistema.
- Painel do menu ganhou fundo arredondado, espaçamento e hierarquia visual mais limpa.
- Ações destrutivas/operacionais ficam separadas e destacadas no menu.
- Nova ação **Pausar visão/análise**: interrompe OCR/detecção temporariamente sem encerrar o serviço.
- Nova ação **Remover sobreposição**: oculta HUD, ícones, liga e touch targets sem encerrar a captura; pode ser restaurada pela notificação.
- Nova ação **Parar serviço PvPPokeGo** disponível no menu.
- Notificação persistente passa a oferecer três ações: Pausar/Retomar visão, Remover/Mostrar sobreposição e Parar serviço.
- Estado da notificação muda conforme visão pausada ou sobreposição oculta.
- Mantidas as correções e recursos da 0.5.13: layout aprovado, identidade estável do time, confiança de golpes e histórico explicável.


## 0.5.13

- Baseada no 0.5.11 validado, preservando ORIGINAL_ICONS, touch zones, rotação e controles entre batalhas.
- Aplica como preset oficial as posições aprovadas pelo usuário sem sobrescrever posições manuais existentes.
- Migração organiza os dois blocos de reserva por ordem visual (card superior = reserva 1, card inferior = reserva 2) sem mudar os lugares na tela.
- Identidade estável dos três Pokémon do time por slot + nome + PC quando disponível.
- Ordem das reservas passa a girar a partir do Pokémon ativo, acompanhando os cards reais do Pokémon GO após cada troca.
- Golpes carregados são resolvidos como um par distinto; o segundo slot não duplica silenciosamente o primeiro.
- Reservas usam o estado/moveset salvo do próprio Pokémon ao calcular matchup e recomendação.
- Nível de confiança de golpe: ✓ confirmado, M manual, ~ estimado pelo ranking e ? desconhecido.
- Histórico registra Pokémon, origem, confiança e justificativa das decisões do assistente.
- Diagnóstico registra mudanças do moveset associado às reservas para facilitar investigação de erros.
- Engrenagem substituída por desenho vetorial fino, no mesmo estilo visual do olho/cadeado.
- Engrenagem mantém pequena margem do canto para não ficar cortada visualmente.
- Backup exportado passa a usar o nome PvPPokeGo-0.5.13-config.json.


## 0.5.11

### Correções recuperadas da base ORIGINAL_ICONS
- Hit-targets do olho e cadeado voltaram a ser transparentes; os ícones delicados são desenhados no canvas, sem emoji do sistema cobrindo o desenho.
- Cadeado aberto usa zonas pequenas por bloco em vez de uma camada fullscreen, deixando ataques/escudos do jogo clicáveis fora do HUD.
- Cadeado, olho, ⚙ e liga usam touch slop do sistema para distinguir toque de arraste.
- Ao iniciar a captura, `EXTRA_APP_VISIBLE=false` evita que os controles fiquem ocultos/sem resposta.
- Barra de edição ✓/↺ é recriada/elevada corretamente e sai do modo edição de forma segura.
- Olho, cadeado e ⚙ continuam acessíveis entre batalhas.
- Posição do topo deixa de seguir o card detectado a cada frame, evitando salto/flicker.
- Captura recria o VirtualDisplay quando a resolução/orientação muda.
- Reinício sticky sem MediaProjection orienta o usuário a iniciar a captura novamente.
- Removido uso inseguro de `windowManager!!` nesse fluxo.


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
