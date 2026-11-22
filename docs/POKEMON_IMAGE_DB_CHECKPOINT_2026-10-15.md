# Banco de imagens Pokémon — checkpoint 2026-10-15

Status: PARCIAL. Catálogo e validação local preparados fora do repositório; **nenhum PNG baixado, nenhum APK compilado e nenhuma integração Android realizada**.

- Catálogo: 1.025 espécies-base + 46 candidatos de Mega (1.071 entradas).
- Testes locais: 5/5 PASS (unicidade de chave, unicidade Dex/forma, cobertura de espécies, distinção Mega X/Y, HTTPS).
- Verificação de arquivos: 0 PNG válido, 1.071 ausentes; nenhum arquivo inválido encontrado.
- Scripts produzidos: `sync_images.py`, `verify_images.py`, `test_catalog.py`.
- Fonte candidata: PokeAPI sprites HOME; formas Mega e licenças de uso exigem revisão.
- Bloqueio: ambiente local sem resolução DNS para github.com/raw.githubusercontent.com.
- Próximos passos: obter imagens em ambiente conectado; verificar cada imagem e forma; integrar cache persistente e LruCache ao atlas Android existente; testes UI, offline, Wi-Fi e build real; atualizar Guia Mestre canônico e espelho Drive após implementação.
- Não marcar como concluído antes de validar PNGs reais e integração.
