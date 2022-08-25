# DiscordAuth

Discord Auth is a minecraft plugin for authorizing players through discord.

`For minecraft 1.17.x - 1.19.x`

**Plugin features**:
- does not allow unauthorized players to interact with the world, write a message and execute commands
- kicks unverified users
- convenient authorization via discord
- protection of the player's account from hacking (not counting hacking of the discord account)
- the speed of the plugin

**Instructions for configuring the plugin**:
- create a discord bot and copy its token ([tutorial](https://github.com/reactiflux/discord-irc/wiki/Creating-a-discord-bot-&-getting-a-token))
- find config.yml and paste the token to `bot-token: ""`
- copy the id of the text channel that the discord bot will listen to
- find config.yml and paste id to `channel-id: ""`

**Instructions for working with the plugin for ordinary users**:
- To add an account, the user must send a message `!add <username>` from the discord server to a special channel
- Log in to the minecraft server
- Discord bot will send a confirmation request to log in to the server. The player must confirm the entry. To confirm the login, you need to react to the message

### Plugin commands
Command **remove_user**:
- usage: `/remove_user <username>`
- description: This command is for deleting a user from the database

Command **reload_discordauth**:
- usage: `/reload_discordauth`
- description: This command to reload plugin

### Discord bot commands
Command **add**:
- usage: `!add <username>`
- description: add account to database

Command **delete**:
- usage: `!delete <username>`
- description: delete account from database

Command **help**:
- usage: `!help`
- description: send info about commands

### Example config.yml:
```yml
bot-token: ""
# listening channel
channel-id: ""

# the maximum number of accounts that a user can have
max-num-of-accounts: 1
# if true, users can delete their accounts
allow-delete-accounts: false

activity:
  # WATCHING, LISTENING, PLAYING, COMPETING
  type: PLAYING
  text: "minecraft"

# time to log in (time in seconds)
auth-time: 60
```

### Example messages.yml:
```yml
# {%username%} - the name of the user who joined
# {%server_name%} - server name
# <c > - specifying the color
# <c0> - black
# <c1> - dark blue
# <c2> - dark green
# <c3> - dark aqua
# <c4> - dark red
# <c5> - dark purple
# <c6> - gold
# <c7> - gray
# <c8> - dark gray
# <c9> - blue
# <ca> - green
# <cb> - aqua
# <cc> - red
# <cd> - light purple
# <ce> - yellow
# <cf> - white

# plugin messages
welcome: "Welcome, {%username%} to the server!"

login:
  logged_in: "<c2>Successful login!"
  log_in: "<cc>Login via discord!"

remove_user:
  user_removed: "<ce>{%username%}<cf> was <c4>removed<cf>!"

error:
  logged_in: "<c4>You are already logged in!"
  not_logged_in: "<c4>You are not logged in!"
  not_authorized: "<c4> You are not registered!"
  arguments: "<c4>You forgot arguments!"
  user_not_exist: "<c4>The user by name <ce>{%username%}<c4> does not exist."
  permissions: "<c4>You don't have permissions!"
  timeout: "<c4>Time out!"

# bot messages
bot:
  verification_successful: "You have been successfully verified!"
  deletion_successful: "The deletion was successful!"
  authorization: "Confirm the login to your {%username%} account!"
  login: "Login completed successfully!"
  help: "1) !add <username> - create account to database\n2) !delete <username> - delete account from database\n3) !help - bot send help"

bot_error:
  user_exists: "Such a user already exists!"
  enough_accounts: "You have enough accounts!"
  name_no_set: "You forgot to set name! Example: !verify Bob"
  login: "Login was not successful!"
  account_deletion_is_not_allowed: "The administrator has forbidden account deletion!"
  account_owner: "You are not the owners of this account!"
  account_not_exits:  "Account don't exits!"
  not_expected_error: "Something went wrong!"
```