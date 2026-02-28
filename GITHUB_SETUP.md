# GitHub Setup Instructions

## Step 1: Create Repository on GitHub

1. Go to: https://github.com/new
2. Fill in:
   - Repository name: `atozshop` (or your preferred name)
   - Description: `Comprehensive shop management system with POS, inventory, e-commerce, and analytics`
   - Visibility: Public or Private
   - **IMPORTANT**: DO NOT check "Initialize with README" (we already have files)
3. Click "Create repository"

## Step 2: Connect Local Repository to GitHub

After creating the repository on GitHub, run these commands in your terminal:

### If your repository is named "atozshop":

```bash
# Add remote origin
git remote add origin https://github.com/hemantmangwani/atozshop.git

# Verify remote
git remote -v

# Push to GitHub
git push -u origin main
```

### If you named it something else (replace REPO_NAME):

```bash
# Add remote origin
git remote add origin https://github.com/hemantmangwani/REPO_NAME.git

# Verify remote
git remote -v

# Push to GitHub
git push -u origin main
```

## Step 3: Verify Upload

After pushing, visit:
`https://github.com/hemantmangwani/atozshop`

You should see all your files:
- README.md
- PROJECT_PLAN.md
- DATABASE_SCHEMA.md
- FEATURE_MATRIX.md
- GETTING_STARTED.md
- PROJECT_SUMMARY.md
- .gitignore
- src/

## Troubleshooting

### If you get authentication error:

**Option A: Use Personal Access Token**
1. Go to: https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Select scopes: `repo` (full control)
4. Generate and copy the token
5. When pushing, use token as password

**Option B: Use SSH (Recommended)**
```bash
# Generate SSH key (if you don't have one)
ssh-keygen -t ed25519 -C "your-email@example.com"

# Copy public key
cat ~/.ssh/id_ed25519.pub

# Add to GitHub:
# Go to: https://github.com/settings/ssh/new
# Paste the key and save

# Change remote to SSH
git remote set-url origin git@github.com:hemantmangwani/atozshop.git

# Push
git push -u origin main
```

### If branch name is "master" instead of "main":

```bash
# Rename branch to main
git branch -M main

# Push
git push -u origin main
```

### If you get "repository already exists" error:

This means you initialized the GitHub repo with files. You'll need to:
```bash
# Pull first
git pull origin main --allow-unrelated-histories

# Then push
git push -u origin main
```

## After Successful Push

Your repository URL will be:
`https://github.com/hemantmangwani/atozshop`

Clone URL:
- HTTPS: `https://github.com/hemantmangwani/atozshop.git`
- SSH: `git@github.com:hemantmangwani/atozshop.git`

## Repository Settings (Recommended)

After pushing, configure your repo:

1. **Add Topics** (for discoverability):
   - Go to repository → "About" (top right)
   - Add topics: `java`, `spring-boot`, `postgresql`, `retail`, `pos`, `inventory-management`, `ecommerce`, `shop-management`

2. **Set Description**:
   "Comprehensive shop management system with POS billing, inventory tracking, e-commerce website, delivery management, and business analytics. Built with Java Spring Boot + PostgreSQL + React/Flutter."

3. **Enable Issues** (for tracking):
   - Settings → Features → Check "Issues"

4. **Add README Preview**:
   GitHub will automatically show README.md on the main page ✅

## Next Steps After Push

1. Share repository URL with your team
2. Set up branch protection rules (optional)
3. Configure GitHub Actions for CI/CD (later phases)
4. Create issues from FEATURE_MATRIX.md
5. Start development!

## Quick Reference

```bash
# Check current status
git status

# Create new branch for feature
git checkout -b feature/user-authentication

# Stage changes
git add .

# Commit
git commit -m "feat: implement user authentication"

# Push
git push origin feature/user-authentication

# Switch back to main
git checkout main

# Pull latest
git pull origin main
```

---

*Happy coding!* 🚀