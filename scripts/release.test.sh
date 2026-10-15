#!/usr/bin/env bash
# Checks release.sh against a local origin and a fake gh: no network, no token.
set -euo pipefail

release=$(cd "$(dirname "$0")" && pwd)/release.sh
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
fail() {
	echo "FAIL: $*" >&2
	exit 1
}

# origin has beta-1 on the first commit and main one commit later. The clone mirrors the
# runner's: main only, no tags.
git init -q -b main "$work/seed"
git -C "$work/seed" -c user.name=t -c user.email=t@t commit -q --allow-empty -m one
git -C "$work/seed" tag beta-1
git -C "$work/seed" -c user.name=t -c user.email=t@t commit -q --allow-empty -m two
git clone -q --bare "$work/seed" "$work/origin.git"
git clone -q --depth=1 --no-tags "file://$work/origin.git" "$work/repo"
cd "$work/repo"
tagged=$(git -C "$work/seed" rev-parse beta-1)
main=$(git rev-parse HEAD)

"$release" checkout beta-2 >/dev/null
[ "$(git rev-parse HEAD)" = "$main" ] || fail "a missing tag moved HEAD"

"$release" checkout beta-1 >/dev/null
[ "$(git rev-parse HEAD)" = "$tagged" ] || fail "an existing tag was not checked out"

git checkout -q main
git remote set-url origin "file://$work/gone.git"
if "$release" checkout beta-1 >/dev/null 2>&1; then fail "an unreachable origin passed as a new tag"; fi

# gh that logs its arguments; `release view` succeeds only when FAKE_RELEASE=exists.
mkdir "$work/bin"
cat >"$work/bin/gh" <<'EOF'
#!/usr/bin/env bash
echo "$*" >>"$GH_LOG"
[ "$1 $2" != "release view" ] || [ "${FAKE_RELEASE:-}" = exists ]
EOF
chmod +x "$work/bin/gh"
export PATH="$work/bin:$PATH" GH_LOG="$work/gh.log"

: >"$GH_LOG"
"$release" publish beta-2 Ofis.dmg
grep -qx "release create beta-2 --target $main --title Ofis beta-2 --prerelease --generate-notes" "$GH_LOG" ||
	fail "a missing release was not created as a pre-release on HEAD: $(cat "$GH_LOG")"
grep -qx "release upload beta-2 Ofis.dmg --clobber" "$GH_LOG" || fail "the file was not uploaded"

: >"$GH_LOG"
FAKE_RELEASE=exists "$release" publish beta-1 Ofis.dmg
if grep -q "release create" "$GH_LOG"; then fail "an existing release was created again"; fi
grep -qx "release upload beta-1 Ofis.dmg --clobber" "$GH_LOG" || fail "the file was not uploaded to the existing release"

echo "release.sh: ok"
