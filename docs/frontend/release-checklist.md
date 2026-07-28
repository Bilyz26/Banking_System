# Frontend Release Checklist

Frontend releases use tags in the form `frontend-vMAJOR.MINOR.PATCH`. The tag
version must exactly match `frontend/package.json`.

## Before tagging

- [ ] Product requirements and accessibility acceptance criteria are satisfied.
- [ ] `npm ci`, `npm run verify`, and `npm run test:e2e` pass.
- [ ] `npm audit --audit-level=high` reports no blocking findings.
- [ ] The frontend image builds and the full Compose smoke test reports `PASS`.
- [ ] Security headers and production identity-provider origins are reviewed.
- [ ] User-visible changes are recorded in `frontend/RELEASE_NOTES.md`.
- [ ] All required pull-request reviews and CI checks are complete.

## Publish

```shell
git tag -a frontend-v0.1.0 -m "Frontend 0.1.0"
git push origin frontend-v0.1.0
```

The frontend release workflow re-runs verification, browser tests, and the
container build. It then publishes the immutable `dist` archive and SHA-256
checksums to a GitHub release.

## After publishing

- [ ] Download the archive and verify its checksum.
- [ ] Deploy the exact released commit/image through the target environment.
- [ ] Run the environment smoke test and verify monitoring.
- [ ] Confirm the previous release remains available for rollback.
- [ ] Record any operational learning in the next release notes.
