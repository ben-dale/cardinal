package uk.co.ridentbyte.model;

public class Vocabulary {

    public Words firstNames, lastNames, places, objects, actions, countries, communications, businessEntities, loremIpsum, emoji;

    public Vocabulary(Words firstNames, Words lastNames, Words places, Words objects, Words actions, Words countries, Words communications, Words businessEntities, Words loremIpsum, Words emoji) {
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.places = places;
        this.objects = objects;
        this.actions = actions;
        this.countries = countries;
        this.communications = communications;
        this.businessEntities = businessEntities;
        this.loremIpsum = loremIpsum;
        this.emoji = emoji;
    }
}
