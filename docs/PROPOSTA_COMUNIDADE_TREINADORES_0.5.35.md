# PvPPokeGo — Perfil de Treinador, Amigos, Duelos, Reides PvE e Eventos
**Proposta registrada:** 2026-10-09 · **Status:** backlog/proposta; NÃO implementado no APK 0.5.34.

## Origem / evidências apresentadas
Duas capturas fornecidas pelo usuário da interface Pokémon GO:
1. Perfil de treinador na aba EU: avatar + Pokémon companheiro, nível/XP, atalhos e estatísticas de atividade.
2. Aba AMIGOS: código de treinador de 12 dígitos, QR de amizade, botões compartilhar/copiar e escanear QR.
**Privacidade:** não transcrever nem inserir em documentação pública ou logs o código de treinador, QR, nome da conta ou qualquer dado pessoal exposto nas capturas. Apenas usar esses campos como referência de formato.

**Intenção do usuário:** aproveitar essas informações no PvPPokeGo para permitir que treinadores se encontrem, compartilhem códigos, proponham duelos, organizem atividades, eventos e PvE/reides.

## Regras oficiais / limitações de integração
- Pokémon GO permite amizade via código de 12 dígitos ou QR oficial (Centro de Ajuda Niantic, FAQ 2938, https://niantic.helpshift.com/hc/pt-br/6-pokemon-go/faq/2938-adding-friends/).
- A batalha local exige escanear o QR chamado **Battle Code**, diferente do **QR de amizade**; desafios remotos usam a lista de amigos no próprio Pokémon GO (FAQ 2308, https://niantic.helpshift.com/hc/en/6-pokemon-go/faq/2308-battling-other-trainers/).
- Reides usam convites feitos **dentro de Pokémon GO**, com regras de passes e limites (FAQ 2601, https://niantic.helpshift.com/hc/en/6-pokemon-go/faq/2601-inviting-friends-to-raid-battles/).
- O Jogo em Grupo usa **código temporário de 9 dígitos** ou QR de grupo, com anfitrião próximo, não é o QR de amizade (FAQ 4312, https://niantic.helpshift.com/hc/pt-br/6-pokemon-go/faq/4312-joining-a-party/).
- **NÃO há integração oficial comprovada** para PvPPokeGo criar amizade, começar batalha, enviar convite de reide ou entrar em grupo diretamente no backend de Pokémon GO. O PvPPokeGo pode organizar, notificar, compartilhar e orientar usuários, mas uma ação no jogo precisa ocorrer no próprio Pokémon GO. Não prometer API nem criar automação não autorizada da interface de outro app.

## UX proposta — aba "Comunidade" (mantendo design clean)
A. **Meu Treinador**
- Apelido de jogo (digitado), nível existente, facção existente, região ampla/cidade opcional e descrição curta.
- Código de treinador: digitar 12 dígitos, colar, importar via captura escolhida explicitamente; validação comprimento e formatação 4+4+4. Persistência privada/local.
- "Copiar código", "Compartilhar cartão" via Android Share Sheet; QR próprio **PvPPokeGo** para abrir cartão no app, sem fingir que QR arbitrário seja reconhecido pelo Pokémon GO. Opção anexar/compartilhar imagem original do QR gerado no jogo, se usuário quiser.
- Avatar/companheiro apenas se foto autorizada; não copiar modelos proprietários dos personagens.
B. **Treinadores e Amigos**
- Adicionar por código/QR PvPPokeGo, editar apelido/observações, marcar favorito, silenciar/bloquear; os contatos geridos no app **não são sincronizados automaticamente** com a lista do jogo.
- Status de pedido somente sobre a rede PvPPokeGo quando existir backend (nunca reportar "amizade no jogo confirmada" sem confirmação explícita do usuário).
C. **Desafiar Duelo (PvP)**
- Criar desafio contendo liga, horário/fuso, tipo (amistoso, treino, melhor de 3), regras de time opcionais, localização/online e mensagem de convite.
- Enviar convite de coordenação por link/texto/canais do usuário; estado local aguardando/aceito/recusado, marcação de placar e estatísticas.
- Botão "Abrir Pokémon GO" ou instrução para desafiar no jogo; não tentar iniciar duelo remoto automaticamente. Em partidas locais, instruir a ler o Battle Code oficial do jogo, que é distinto do QR de amizade.
D. **Reides e PvE**
- Preparar equipe recomendada pela tipagem/banco local, nível de interesse, treinador disponível, item/lobby como anotação sem chaves/códigos sensíveis.
- Criar grupo de preparação com horário, vagas, raid boss, localidade aproximada ou remoto, indicando que convites e passes são gerenciados pelo Pokémon GO.
E. **Eventos e Grupos**
- Convite para encontro/copa, inscrições, agenda por região, limite de participantes, resultados e ferramentas de exportação.
- Não apresentar grupos da comunidade como o Jogo em Grupo oficial: código temporário/nativo deve vir do jogo.
F. **Privacidade e segurança**
- Perfil privado por padrão; nenhum código/região revelado em feed público sem opção clara; controle de visibilidade granular.
- Nunca subir screenshot/QR original, lista de contatos do celular, posição GPS precisa ou dados de outros jogadores sem consentimento.
- Bloqueio, denúncia, exclusão/exportação de dados, proteção para contas de menores e moderação antes de habilitar feed aberto, localização e mensagens.
- Usar UUID local e evitar tratar código Pokémon GO como autenticação ou prova de propriedade da conta.
- Sincronização multiusuário/push exige arquitetura de backend e autenticação próprias, além de regras de segurança/abuso e LGPD. Sem isso, Fase 1 só trabalha localmente, share sheet e importação de cartões.

## Etapas de implementação sugeridas
**Fase A — candidato 0.5.35 local (P0):** adicionar código/nick ao `TrainerPreferencesRepository` (atualmente contém nível e facção), tela Minha Identidade com copiar/compartilhar cartão e cadastro voluntário por screenshot usando OCR existente; validação robusta do código, backup seletivo opt-in, testes para evitar vazamento em logs/diagnóstico. Agenda de duelos/reides local com share sheet e lembretes apenas se autorizado. Sem backend.
**Fase B — social online (P1):** acordar backend com cadastro, consentimento e moderação; convites entre usuários PvPPokeGo, RSVP, grupos, bloqueio/denúncias, notificações, limites por sessão/antispam. Requer infraestrutura, credenciais e política de privacidade.
**Fase C — integração de estratégia (P2):** seleção equipe escaneada 1/2/3 para desafios internos, relatórios de duelo, indicação de melhores times PvE e meta de eventos a partir do motor PvPoke existente. Foco em colaboração e educação, nunca intervenção/automação clandestina no Pokémon GO.

## Critérios de aceite para quando virar código
- Cadastro de 12 dígitos (sem alterar código real e sem publicar sem confirmação); colar e limpar; importar screenshot com prévia, conferir manualmente.
- Copiar/compartilhar só após toque; privacidade por padrão, sem dados pessoais em logs/CI/telemetria.
- Duelo/raid organizado no app deve ser **rótulo "convite de coordenação"**; nunca afirmar que envio foi entregue no Pokémon GO.
- QR de amizade, Battle QR e QR/código de Party Play visualmente e semanticamente distintos.
- Manter todo o código da v0.5.34 e anteriores (overlay, reserva, reconhecimento, HUD, scanner, Logcat, HP, backups).
- Documentar branch/HEAD/testes/artefato em todo release; não gerar APK até implementação e QA. O escopo social NÃO está na 0.5.34.
