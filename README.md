# pokemongosocial
Pokemon Go: Social!

The Android App for socializing with fellow Pokemon Go players.

## Dependencies:

Uses [Volley](https://developer.android.com/training/volley/index.html) for network requests.
Uses [SearchableSpinner](https://github.com/miteshpithadiya/SearchableSpinner)
Run 'gradlew' to get the stuff

## Database Schema (since only I can see that right now):

###Users:
- username
- password (hashed)
- profile_image_path (should be just <TEAM>_pic.jpg but I left it here just in case)
- team (Instinct, Valor, or Mystic)
- time_joined (DATE_TIME)
- reputation (integer, like karma)

###Posts:
- post_id (int, primary key auto increment)
- user_id
- title
- caption
- time
- latitude
- longitude
- likes (positive or negative based on thumbs up/down)
- user_team
- only_visible_team (1 or 0 if it is public or only for their team)

###Actions: (really just comments but used to have likes and comments til I changed that)
- action_id (int, primary key auto increment)
- user_id
- post_id
- content
- likes
- time
