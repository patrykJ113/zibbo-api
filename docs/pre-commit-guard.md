# 🔐 Environment variables

This project keeps secrets (DB password, etc.) in a `.env` file. The values are
**encrypted** with [Dotenvx](https://dotenvx.com), so the `.env` file is safe to
commit — nobody can read the secrets without the private key.

A **pre-commit hook** (built into Dotenvx) makes sure you never accidentally
commit a `.env` with plaintext (unencrypted) secrets.

---

## What you need to install

### Dotenvx — encrypts/decrypts the `.env`

```bash
npm install -g @dotenvx/dotenvx
```

That's the only tool required. The pre-commit check is part of Dotenvx — no
Python, no `pre-commit` framework, no custom scripts.

---

## One-time setup after cloning

**1. Get the private key.**
To decrypt the `.env` and run the app, you need the **`.env.keys`** file. It is
**not** in the repo (it's gitignored on purpose). Get it from the project owner
and place it in the project root.

> The private key is the one thing that must stay secret. If it leaked, all the
> encrypted values could be read.

**2. Install the pre-commit hook.**

```bash
dotenvx ext precommit --install
```

This wires a check into git so it runs automatically on every commit. Git hooks
don't come with a clone, so **each person must run this once** after cloning.

---

## Daily use

**Add or change a secret** — use `dotenvx set` so it's encrypted right away:

```bash
dotenvx set DB_PASSWORD yourpassword
```

**Read a value** (decrypted):

```bash
dotenvx get DB_PASSWORD
```

**Run the app with decrypted values injected:**

```bash
dotenvx run -- mvn spring-boot:run
```

> In IntelliJ, the run configuration has **Enable Dotenvx** ticked, so running
> from the IDE decrypts and injects the values automatically.

---

## What happens when you commit

Every `git commit` triggers the hook. It checks that all `.env` files are
protected (encrypted or gitignored):

- ✅ **Protected** → commit goes through
- ❌ **Plaintext secret found** → commit blocked, with a hint on how to fix it

If you get blocked, encrypt and commit again:

```bash
dotenvx encrypt
git add .env
git commit -m "your message"
```

You can also run the check manually anytime:

```bash
dotenvx ext precommit
```

---

## How the encryption works (quick reference)

- Each value is encrypted with **ECIES** (secp256k1 + AES-256-GCM).
- Format in the file: `KEY="encrypted:<base64 blob>"`.
- The public key (`DOTENV_PUBLIC_KEY`) lives in `.env` and encrypts values.
- The private key (`.env.keys`) decrypts them and is **never committed**.
- The same value encrypts to a **different blob each time** — that's expected
  (fresh random key per encryption).