package com.chague.bibliotheque.infrastructure.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SeedUsersFile {
    private List<SeedUserItem> users = new ArrayList<>();
}
