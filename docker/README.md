### Docker context layout

```
name/version
    - Dockerfile
    - tag   # contains the complete repo/image:tag
    - make.sh
    - .gitignore
```

### Build an image

```sh
cd ./name/version && ./make.sh
```
