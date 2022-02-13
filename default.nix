{ pkgs ? import <nixpkgs> {} }: with pkgs;
stdenv.mkDerivation rec {
  version = "0.1";
  pname = "editor";
  src = ./.;
  buildInputs = [
    leiningen
  ];
  # buildPhase = "ghc --make xmonadctl.hs";
  # installPhase = ''
  #   mkdir -p $out/bin
  #   cp xmonadctl $out/bin/
  #   chmod +x $out/bin/xmonadctl
  # '';
  meta = with lib; {
    author = "Luca Leon Happel";
    description = "VIM like editor written in Clojure";
    homepage = "https://github.com/Quoteme/editor";
    platforms = platforms.all;
    mainProgram = "editor";
  };  
}
